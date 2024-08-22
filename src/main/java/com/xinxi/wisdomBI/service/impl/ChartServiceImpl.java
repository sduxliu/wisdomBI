package com.xinxi.wisdomBI.service.impl;

import cn.hutool.core.io.FileUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.github.rholder.retry.Retryer;
import com.github.rholder.retry.RetryerBuilder;
import com.github.rholder.retry.StopStrategies;
import com.github.rholder.retry.WaitStrategies;
import com.xinxi.wisdomBI.bizmq.BiMessageProducer;
import com.xinxi.wisdomBI.common.ErrorCode;
import com.xinxi.wisdomBI.constant.AIConstant;
import com.xinxi.wisdomBI.exception.BusinessException;
import com.xinxi.wisdomBI.exception.ThrowUtils;
import com.xinxi.wisdomBI.manager.AiManager;
import com.xinxi.wisdomBI.manager.RedisLimiterManager;
import com.xinxi.wisdomBI.mapper.ChartMapper;
import com.xinxi.wisdomBI.model.dto.chart.GenChartByAiRequest;
import com.xinxi.wisdomBI.model.entity.Chart;
import com.xinxi.wisdomBI.model.entity.User;
import com.xinxi.wisdomBI.model.enums.ChartStatusEnum;
import com.xinxi.wisdomBI.model.vo.BiResponse;
import com.xinxi.wisdomBI.service.ChartService;
import com.xinxi.wisdomBI.service.UserService;
import com.xinxi.wisdomBI.utils.ExcelUtils;
import com.xinxi.wisdomBI.utils.JSCleanUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 *
 * @author 蒲月理想
 */
@Service
@Slf4j
public class ChartServiceImpl extends ServiceImpl<ChartMapper, Chart>
    implements ChartService {

    @Resource
    AiManager aiManager;

    @Resource
    private UserService userService;
    // 限流
    @Resource
    private RedisLimiterManager redisLimiterManager;
    // 线程池
    @Resource
    private ThreadPoolExecutor threadPoolExecutor;
    // 消息队列-RabbitMQ
    @Resource
    private BiMessageProducer biMessageProducer;

    // AI 结果的格式校验
    @Resource
    private JSCleanUtils jsCleanUtils;
    /**
     * 同步生成
     * @param genChartByAiRequest
     * @param multipartFile
     * @param request
     * @return
     */
    @Override
    public BiResponse genChartByAi(GenChartByAiRequest genChartByAiRequest, MultipartFile multipartFile, HttpServletRequest request){
        // 校验输入数据,保存基础图表数据，限流，获取结果
        InitInputChart initData = getInitInput(genChartByAiRequest, multipartFile, request);
        // 保存生成的图表信息——更新
        return getAiResponseAndSaveChart(initData.userInput, initData.chart);
    }

    @NotNull
    private InitInputChart getInitInput(GenChartByAiRequest genChartByAiRequest, MultipartFile multipartFile, HttpServletRequest request) {
        checkRequest( genChartByAiRequest.getName(),  genChartByAiRequest.getGoal(), multipartFile);
        // 构造用户输入
        String csvData = ExcelUtils.excelToCsv(multipartFile);
        // 获取AI提问信息
        StringBuilder userInput = getMessage( genChartByAiRequest.getGoal(), genChartByAiRequest.getChartType(), csvData);
        // 获取登录用户
        User loginUser = userService.getLoginUser(request);
        // 限流判断，每个用户一个限流器
        redisLimiterManager.doRateLimit("genChartByAi_" + loginUser.getId());

        // 先保存基本图表信息
        Chart chart = saveChart(genChartByAiRequest,loginUser,csvData);
        return new InitInputChart(userInput, chart);
    }

    private static class InitInputChart {
        public final StringBuilder userInput;
        public final Chart chart;

        public InitInputChart(StringBuilder userInput, Chart chart) {
            this.userInput = userInput;
            this.chart = chart;
        }
    }

    private BiResponse getAiResponseAndSaveChart(StringBuilder userInput, Chart chart) {
        String result = aiManager.doSyncStableRequest(AIConstant.PROMPT,userInput.toString());
        String[] splits = result.split("【【【【【");
        log.info("AI生成结果分析:{}", Arrays.toString(splits));
        if (splits.length < 3) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "AI 生成错误");
        }
        String genChart = splits[1].trim();
        // 处理多余的符号，错误的单引号（后续继续完善）
        genChart = JSCleanUtils.cleanJsonString(genChart);

        if(!JSCleanUtils.isValidJson(genChart)){
            this.handleChartUpdateError(chart.getId(), "AI 生成错误");
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "AI 生成错误,请重试");
        }
        String genResult = splits[2].trim();
        // 插入到数据库
        chart.setGenChart(genChart);
        chart.setGenResult(genResult);
        // 设置图表更新状态
        chart.setChartStatus(ChartStatusEnum.SUCCEED.getValue());
        boolean upDataResult = updateById(chart);
        ThrowUtils.throwIf(!upDataResult, ErrorCode.SYSTEM_ERROR, "图表更新失败");
        // 修该后再次判断
        BiResponse biResponse = new BiResponse();
        biResponse.setGenChart(genChart);
        biResponse.setGenResult(genResult);
        biResponse.setChartId(chart.getId());
        return biResponse;
    }

    @Override
    public BiResponse genChartByAiAsync(GenChartByAiRequest genChartByAiRequest, MultipartFile multipartFile, HttpServletRequest request) {
        // 校验输入数据,保存基础图表数据，获取结果
        InitInputChart initData = getInitInput(genChartByAiRequest, multipartFile, request);

        // 然后将任务提交到线程池中，并返回任务ID给前端。前端可以通过任务ID查询任务状态、结果。

        // todo 1. 处理任务队列满了后，抛异常的情况——方案：交任务存储到数据库，然后抛异常
        // TODO 2. 处理任务执行失败的情况——方案：重试2次之后，如果失败记录失败（在AI生成中解决）
        CompletableFuture.runAsync(() -> {
            // 先修改图表任务状态为 “执行中”。等执行成功后，修改为 “已完成”、保存执行结果；执行失败后，状态修改为 “失败”，记录任务失败信息。
            Chart updateChart = new Chart();
            updateChart.setId(initData.chart.getId());
            updateChart.setChartStatus(ChartStatusEnum.RUNNING.getValue());
            boolean b = updateById(updateChart);
            if (!b) {
                handleChartUpdateError(initData.chart.getId(), "更新图表执行中状态失败");
                return;
            }
            // 调用 AI 并且更新图表信息（数据库）：引入AI生成失败的情况，增加重试机制
            boolean updateResult = aiGenChartAndUpdateOldChartInfo(initData.userInput, initData.chart);
            if (!updateResult) {
                this.handleChartUpdateError( initData.chart.getId(), "更新图表成功状态失败");
            }
        }, threadPoolExecutor);

        BiResponse biResponse = new BiResponse();
        biResponse.setChartId(initData.chart.getId());
        return biResponse;
    }

    @Override
    public BiResponse genChartByAiAsyncByMQ(GenChartByAiRequest genChartByAiRequest, MultipartFile multipartFile, HttpServletRequest request) {
        // 校验输入数据,保存基础图表数据，获取结果
        InitInputChart initData = getInitInput(genChartByAiRequest, multipartFile, request);
//        //向消息队列发送消息
        biMessageProducer.sendMessage(String.valueOf(initData.chart.getId()));
        BiResponse biResponse = new BiResponse();
        biResponse.setChartId(initData.chart.getId());
        return biResponse;
    }

    @Override
    public void handleChartUpdateError(long chartId, String execMessage) {
        Chart updateChartResult = new Chart();
        updateChartResult.setId(chartId);
        updateChartResult.setChartStatus(ChartStatusEnum.FAIL.getValue());
        updateChartResult.setExecMessage(execMessage);
        boolean updateResult = this.updateById(updateChartResult);
        if (!updateResult) {
            log.error("更新图表失败状态失败" + chartId + "," + execMessage);
        }
    }
    @Override
    public StringBuilder getMessage(String goal, String chartType, String csvData) {
        // 构造用户输入
        StringBuilder userInput = new StringBuilder();
        userInput.append("分析需求：").append("\n");

        // 拼接分析目标
        String userGoal = goal;
        if (StringUtils.isNotBlank(chartType)) {
            userGoal += "，请使用" + chartType;
        }
        userInput.append(userGoal).append("\n");
        userInput.append("原始数据：").append("\n");
        // 拼接CSV数据
        userInput.append(csvData).append("\n");
        return userInput;
    }
    @Override
    public void checkRequest(String name, String goal, MultipartFile multipartFile) {
        // 校验
        ThrowUtils.throwIf(StringUtils.isBlank(goal), ErrorCode.PARAMS_ERROR, "目标为空");
        ThrowUtils.throwIf(StringUtils.isNotBlank(name) && name.length() > 100, ErrorCode.PARAMS_ERROR, "名称过长");
       // 文件为空：
        ThrowUtils.throwIf(multipartFile == null, ErrorCode.PARAMS_ERROR, "文件为空");
        // 校验文件
        long size = multipartFile.getSize();
        String originalFilename = multipartFile.getOriginalFilename();
        // 校验文件大小
        final long ONE_MB = 1024 * 1024L;
        ThrowUtils.throwIf(size > ONE_MB, ErrorCode.PARAMS_ERROR, "文件超过 1M");
        // 校验文件后缀 aaa.png
        String suffix = FileUtil.getSuffix(originalFilename);
        final List<String> validFileSuffixList = Arrays.asList("xlsx", "xls","XLSX", "XLS");
        ThrowUtils.throwIf(!validFileSuffixList.contains(suffix), ErrorCode.PARAMS_ERROR, "文件后缀非法");

    }
    @Override
    public Chart saveChart(GenChartByAiRequest genChartByAiRequest, User loginUser, String csvData) {
        String name = genChartByAiRequest.getName();
        String goal = genChartByAiRequest.getGoal();
        String chartType = genChartByAiRequest.getChartType();
        // 插入到数据库
        Chart chart = new Chart();
        chart.setName(name);
        chart.setGoal(goal);
        chart.setChartData(csvData);
        chart.setChartType(chartType);
        chart.setChartStatus(ChartStatusEnum.WATT.getValue());
        chart.setUserId(loginUser.getId());
        boolean saveResult = this.save(chart);
        ThrowUtils.throwIf(!saveResult, ErrorCode.SYSTEM_ERROR, "图表保存失败");
        return chart;
    }

    @Override
    public boolean aiGenChartAndUpdateOldChartInfo(StringBuilder userInput, Chart chart) {
      // AI 生成重试机制：失败后重试两次,时间间隔1s，直到成功或者次数耗尽
        Retryer<Boolean> success = RetryerBuilder.<Boolean>newBuilder()
                .retryIfExceptionOfType(Exception.class)
                .retryIfResult(result -> !result)
                .withStopStrategy(StopStrategies.stopAfterAttempt(2))
                .withWaitStrategy(WaitStrategies.fixedWait(1000, TimeUnit.MILLISECONDS))
                .build();

        try{
            success.call(() ->{
                String result = aiManager.doSyncStableRequest(AIConstant.PROMPT, userInput.toString());
                String[] splits = result.split("【【【【【");
                if (splits.length < 3) {
                    this.handleChartUpdateError(chart.getId(), "AI 生成错误");
                    return false;
                }
                String genChart = splits[1].trim();
                // 处理多余的符号，错误的单引号（后续继续完善）
                genChart = JSCleanUtils.cleanJsonString(genChart);
                // 修该后再次判断
                if(!JSCleanUtils.isValidJson(genChart)){
                    this.handleChartUpdateError(chart.getId(), "AI 生成错误");
                    return false;
                }
                String genResult = splits[2].trim();
                Chart updateChartResult = new Chart();
                updateChartResult.setId(chart.getId());
                updateChartResult.setGenChart(genChart);
                updateChartResult.setGenResult(genResult);
                // 更新图表信息
                updateChartResult.setChartStatus(ChartStatusEnum.SUCCEED.getValue());
                return this.updateById(updateChartResult);
            });

        } catch (Exception e) {
            // 重试2次之后
            log.error("重试两次之后,AI调用错误", e);
            return false;
        }
        // 默认返回
        return true;
    }

    @Override
    public Boolean regenerateChart(Long id, Long loginUserId) {
        // 根据图表ID查询原始图标信息
        Chart chart = this.getById(id);
        // 判断图表是否存在
        if (chart == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR);
        }
        // 限流
        chart.setChartStatus(ChartStatusEnum.RUNNING.getValue());
        chart.setGenChart(null);
        chart.setGenResult(null);
        updateById(chart);
        // 获取AI提问信息
        StringBuilder userInput = getMessage( chart.getGoal(), chart.getChartType(), chart.getChartData());
        // 限流判断，每个用户一个限流器
        redisLimiterManager.doRateLimit("genChartByAi_" + loginUserId);
        // 异步生成图表：
        biMessageProducer.sendMessage(String.valueOf(chart.getId()));
        return true;
    }
}




