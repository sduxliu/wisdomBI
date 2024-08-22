package com.xinxi.wisdomBI.controller;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.io.FileUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xinxi.wisdomBI.bizmq.BiMessageProducer;
import com.xinxi.wisdomBI.common.BaseResponse;
import com.xinxi.wisdomBI.common.DeleteRequest;
import com.xinxi.wisdomBI.common.ErrorCode;
import com.xinxi.wisdomBI.common.ResultUtils;
import com.xinxi.wisdomBI.constant.AIConstant;
import com.xinxi.wisdomBI.constant.CommonConstant;
import com.xinxi.wisdomBI.exception.BusinessException;
import com.xinxi.wisdomBI.exception.ThrowUtils;
import com.xinxi.wisdomBI.manager.AiManager;
import com.xinxi.wisdomBI.manager.RedisLimiterManager;
import com.xinxi.wisdomBI.model.dto.chart.*;
import com.xinxi.wisdomBI.model.entity.Chart;
import com.xinxi.wisdomBI.model.entity.User;
import com.xinxi.wisdomBI.model.enums.ChartStatusEnum;
import com.xinxi.wisdomBI.model.vo.BiResponse;
import com.xinxi.wisdomBI.model.vo.ChartVO;
import com.xinxi.wisdomBI.service.ChartService;
import com.xinxi.wisdomBI.service.UserService;
import com.xinxi.wisdomBI.utils.ExcelUtils;
import com.xinxi.wisdomBI.utils.SqlUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import scala.Char;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.stream.Collectors;

/**
 *图表生成控制器
 * @author 蒲月理想
 */
@RestController
@RequestMapping("/chart")
@Slf4j
public class ChartController {

    @Resource
    private ChartService chartService;

    @Resource
    private UserService userService;

    /**
     * 创建图表 JS  AI
     */
    @Resource
    private AiManager aiManager;

    // 限流器
    @Resource
    private RedisLimiterManager redisLimiterManager;

    @Resource
    private ThreadPoolExecutor threadPoolExecutor;

    @Resource
    private BiMessageProducer biMessageProducer;


    // region 增删改查

    /**
     * 创建
     *
     * @param chartAddRequest
     * @param request
     * @return
     */
    @PostMapping("/add")
    public BaseResponse<Long> addChart(@RequestBody ChartAddRequest chartAddRequest, HttpServletRequest request) {
        if (chartAddRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        Chart chart = new Chart();

        // 仅管理员可创建
        User loginUser = userService.getLoginUser(request);
        if (!userService.isAdmin(loginUser)) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
        }
        BeanUtils.copyProperties(chartAddRequest, chart);
        chart.setUserId(loginUser.getId());
        boolean result = chartService.save(chart);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
        long newChartId = chart.getId();
        return ResultUtils.success(newChartId);
    }

    /**
     * 删除
     *
     * @param deleteRequest
     * @param request
     * @return
     */
    @PostMapping("/delete")
    public BaseResponse<Boolean> deleteChart(@RequestBody DeleteRequest deleteRequest, HttpServletRequest request) {
        if (deleteRequest == null || deleteRequest.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User user = userService.getLoginUser(request);
        long id = deleteRequest.getId();
        // 判断是否存在
        Chart oldChart = chartService.getById(id);
        ThrowUtils.throwIf(oldChart == null, ErrorCode.NOT_FOUND_ERROR);
        // 仅本人或管理员可删除
        if (!oldChart.getUserId().equals(user.getId()) && !userService.isAdmin(request)) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
        }
        boolean b = chartService.removeById(id);
        return ResultUtils.success(b);
    }

    /**
     * 更新（仅管理员）
     *
     * @param chartUpdateRequest
     * @return
     */
    @PostMapping("/update")
    public BaseResponse<Boolean> updateChart(@RequestBody ChartUpdateRequest chartUpdateRequest, HttpServletRequest request) {
        if (chartUpdateRequest == null || chartUpdateRequest.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        // 仅管理员可更新
        if (!userService.isAdmin(request)) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
        }
        Chart chart = new Chart();
        BeanUtils.copyProperties(chartUpdateRequest, chart);
        long id = chartUpdateRequest.getId();
        // 判断是否存在
        Chart oldChart = chartService.getById(id);
        ThrowUtils.throwIf(oldChart == null, ErrorCode.NOT_FOUND_ERROR);
        boolean result = chartService.updateById(chart);
        return ResultUtils.success(result);
    }
/**
 * 根据id更新图表，更新生成的图表
 */
@PostMapping("/update/gen")
public BaseResponse<Boolean> updateChartByGen(@RequestBody ChartUpdateRequest chartUpdateRequest, HttpServletRequest request) {
    if (chartUpdateRequest == null || chartUpdateRequest.getId() <= 0) {
        throw new BusinessException(ErrorCode.PARAMS_ERROR);
    }
    // 更新
    Chart chart = new Chart();
    BeanUtils.copyProperties(chartUpdateRequest, chart);
    return ResultUtils.success(chartService.updateById(chart));
}
    /**
     * 根据 id 获取
     *
     * @param id
     * @return
     */
    @GetMapping("/get")
    public BaseResponse<Chart> getChartById(long id, HttpServletRequest request) {
        log.info("id="+id);
        if (id <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        Chart chart = chartService.getById(id);
        if (chart == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR);
        }
        return ResultUtils.success(chart);
    }

    /**
     * 分页获取列表（封装类）——管理员才查询所有
     *
     * @param chartQueryRequest
     * @param request
     * @return
     */
    @PostMapping("/list/page")
    public BaseResponse<Page<Chart>> listChartByPage(@RequestBody ChartQueryRequest chartQueryRequest,
                                                     HttpServletRequest request) {
        User loginUser = userService.getLoginUser(request);
        if(!userService.isAdmin(loginUser)){
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR,"没有权限");
        }
        // 如果未登录：返回
        if (loginUser == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR, "请先登录");
        }
        long current = chartQueryRequest.getCurrent();
        long size = chartQueryRequest.getPageSize();
        // 限制爬虫
        ThrowUtils.throwIf(size > 20, ErrorCode.PARAMS_ERROR);
        Page<Chart> chartPage = chartService.page(new Page<>(current, size),
                getQueryWrapper(chartQueryRequest));
        return ResultUtils.success(chartPage);
    }

    /**
     * 分页获取当前用户创建的资源列表
     *
     * @param chartQueryRequest
     * @param request
     * @return
     */
    @PostMapping("/my/list/page")
    public BaseResponse<Page<ChartVO>> listMyChartByPage(@RequestBody ChartQueryRequest chartQueryRequest, HttpServletRequest request) {

        if (chartQueryRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User loginUser = userService.getLoginUser(request);
        // 如果未登录：返回
        if (loginUser == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR, "请先登录");
        }
        log.info("搜索参数为："+chartQueryRequest);
        chartQueryRequest.setUserId(loginUser.getId());
        long current = chartQueryRequest.getCurrent();
        long size = chartQueryRequest.getPageSize();
        log.info("开始查询用户：{}", loginUser.getId());
        // 限制爬虫
        ThrowUtils.throwIf(size > 20, ErrorCode.PARAMS_ERROR);
        // 查询数据，转换为ChartVOO：
        Page<Chart> chartPage = chartService.page(new Page<>(current, size), getQueryWrapper(chartQueryRequest));
       // 数据转换
        Page<ChartVO> chartVOPage = new Page<>(current, size, chartPage.getTotal());
        List<ChartVO> chartVOList = new ArrayList<>();
        for (Chart chart : chartPage.getRecords()) {
           ChartVO chartVO = new ChartVO();
            BeanUtils.copyProperties(chart, chartVO);
            chartVOList.add(chartVO);
        }
        chartVOPage.setRecords(chartVOList);
        log.info("查询数据转换：{}", chartVOPage.getRecords());
//        Page<Chart> chartPage = chartService.page(new Page<>(current, size),
//                getQueryWrapper(chartQueryRequest));
        return ResultUtils.success(chartVOPage);
    }

    // endregion

    /**
     * 编辑（用户）
     *
     * @param chartEditRequest
     * @param request
     * @return
     */
    @PostMapping("/edit")
    public BaseResponse<Boolean> editChart(@RequestBody ChartEditRequest chartEditRequest, HttpServletRequest request) {
        if (chartEditRequest == null || chartEditRequest.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        Chart chart = new Chart();
        BeanUtils.copyProperties(chartEditRequest, chart);
        User loginUser = userService.getLoginUser(request);
        long id = chartEditRequest.getId();
        // 判断是否存在
        Chart oldChart = chartService.getById(id);
        ThrowUtils.throwIf(oldChart == null, ErrorCode.NOT_FOUND_ERROR);
        // 仅本人或管理员可编辑
        if (!oldChart.getUserId().equals(loginUser.getId()) && !userService.isAdmin(loginUser)) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
        }
        boolean result = chartService.updateById(chart);
        return ResultUtils.success(result);
    }

    /**
     * 重新生成图表
     */
    @GetMapping("/regenerate")
    public BaseResponse<Boolean> regenerateChart(@RequestParam("id") Long id, HttpServletRequest request) {
        // 重新生成
        User loginUser = userService.getLoginUser(request);
        if(loginUser == null){
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR);
        }
        return ResultUtils.success( chartService.regenerateChart(id,loginUser.getId()));
    }
    /**
     * 智能分析（同步）
     *
     * @param multipartFile
     * @param genChartByAiRequest
     * @param request
     * @return
     */
    @PostMapping("/gen")
    public BaseResponse<BiResponse> genChartByAi(@RequestPart("file") MultipartFile multipartFile,
                                                 GenChartByAiRequest genChartByAiRequest, HttpServletRequest request) {

       BiResponse biResponse =  chartService.genChartByAi(genChartByAiRequest, multipartFile, request);
//        String name = genChartByAiRequest.getName();
//        String goal = genChartByAiRequest.getGoal();
//        String chartType = genChartByAiRequest.getChartType();
//        chartService.checkRequest(name, goal, multipartFile);
//
//        // 获取登录用户
//        User loginUser = userService.getLoginUser(request);
//        // 限流判断，每个用户一个限流器
//        redisLimiterManager.doRateLimit("genChartByAi_" + loginUser.getId());
//        // 构造用户输入
//        String csvData = ExcelUtils.excelToCsv(multipartFile);
//        StringBuilder userInput = chartService.getMessage(goal, chartType, csvData);
//
//
//        String result = aiManager.doSyncStableRequest(AIConstant.PROMPT,userInput.toString());
//        String[] splits = result.split("【");
//        if (splits.length < 3) {
//            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "AI 生成错误");
//        }
//        String genChart = splits[1].trim();
//        String genResult = splits[2].trim();
//        // 插入到数据库
//        Chart chart = new Chart();
//        chart.setName(name);
//        chart.setGoal(goal);
//        chart.setChartData(csvData);
//        chart.setChartType(chartType);
//        chart.setGenChart(genChart);
//        chart.setGenResult(genResult);
//        chart.setUserId(loginUser.getId());
//        // 设置图表更新状态
//        chart.setStatus(ChartStatusEnum.SUCCEED.getValue());
//        boolean saveResult = chartService.save(chart);
//        ThrowUtils.throwIf(!saveResult, ErrorCode.SYSTEM_ERROR, "图表保存失败");
//        BiResponse biResponse = new BiResponse();
//        biResponse.setGenChart(genChart);
//        biResponse.setGenResult(genResult);
//        biResponse.setChartId(chart.getId());
        return ResultUtils.success(biResponse);
    }
    

    /**
     * 智能分析（异步）
     *
     * @param multipartFile
     * @param genChartByAiRequest
     * @param request
     * @return
     */
    @PostMapping("/gen/async")
    public BaseResponse<BiResponse> genChartByAiAsync(@RequestPart("file") MultipartFile multipartFile,
                                                      GenChartByAiRequest genChartByAiRequest, HttpServletRequest request) {

        BiResponse biResponse = chartService.genChartByAiAsync(genChartByAiRequest, multipartFile, request);
//        String name = genChartByAiRequest.getName();
//        String goal = genChartByAiRequest.getGoal();
//        String chartType = genChartByAiRequest.getChartType();
//        //校验
//        chartService.checkRequest(name, goal, multipartFile);
//        // 获取登录用户
//        User loginUser = userService.getLoginUser(request);
//        // 限流判断，每个用户一个限流器
//        redisLimiterManager.doRateLimit("genChartByAi_" + loginUser.getId());
//        // 构造用户输入
//        String csvData = ExcelUtils.excelToCsv(multipartFile);
//        StringBuilder userInput = chartService.getMessage(goal, chartType, csvData);
//        // 异步先保存图表原始信息到数据库中：
//        Chart chart = chartService.saveChart(genChartByAiRequest, loginUser,csvData);
//
//        // 然后将任务提交到线程池中，并返回任务ID给前端。前端可以通过任务ID查询任务状态、结果。
//
//        // todo 建议处理任务队列满了后，抛异常的情况
//        CompletableFuture.runAsync(() -> {
//            // 先修改图表任务状态为 “执行中”。等执行成功后，修改为 “已完成”、保存执行结果；执行失败后，状态修改为 “失败”，记录任务失败信息。
//            Chart updateChart = new Chart();
//            updateChart.setId(chart.getId());
//            updateChart.setStatus(ChartStatusEnum.RUNNING.getValue());
//            boolean b = chartService.updateById(updateChart);
//            if (!b) {
//                chartService.handleChartUpdateError(chart.getId(), "更新图表执行中状态失败");
//                return;
//            }
//            // 调用 AI 并且更新图表信息（数据库）
//            chartService.aiGenChartAndUpdateOldChartInfo(userInput, chart);
//        }, threadPoolExecutor);
//
//        BiResponse biResponse = new BiResponse();
//        biResponse.setChartId(chart.getId());
        return ResultUtils.success(biResponse);
    }




    /**
     * 智能分析（异步消息队列）
     *
     * @param multipartFile
     * @param genChartByAiRequest
     * @param request
     * @return
     */
    @PostMapping("/gen/async/mq")
    public BaseResponse<BiResponse> genChartByAiAsyncMq(@RequestPart("file") MultipartFile multipartFile,
                                                        GenChartByAiRequest genChartByAiRequest, HttpServletRequest request) {
       BiResponse biResponse = chartService.genChartByAiAsyncByMQ(genChartByAiRequest,multipartFile,request);
//        String name = genChartByAiRequest.getName();
//        String goal = genChartByAiRequest.getGoal();
//        String chartType = genChartByAiRequest.getChartType();
//        //校验
//        chartService.checkRequest(name, goal, multipartFile);
//        // 获取登录用户
//        User loginUser = userService.getLoginUser(request);
//        // 限流判断，每个用户一个限流器
//        redisLimiterManager.doRateLimit("genChartByAi_" + loginUser.getId());
//        // 构造用户输入
//        String csvData = ExcelUtils.excelToCsv(multipartFile);
//        StringBuilder userInput = chartService.getMessage(goal, chartType, csvData);
//        // 异步先保存图表原始信息到数据库中：
//        Chart chart = chartService.saveChart(genChartByAiRequest, loginUser,csvData);
//        long newChartId = chart.getId();
//        //向消息队列发送消息
//        biMessageProducer.sendMessage(String.valueOf(newChartId));
//
//        BiResponse biResponse = new BiResponse();
//        biResponse.setChartId(newChartId);
        return ResultUtils.success(biResponse);
    }

    


    /**
     * 获取查询包装类
     *
     * @param chartQueryRequest
     * @return
     */
    private QueryWrapper<Chart> getQueryWrapper(ChartQueryRequest chartQueryRequest) {
        QueryWrapper<Chart> queryWrapper = new QueryWrapper<>();
        if (chartQueryRequest == null) {
            return queryWrapper;
        }
        Long id = chartQueryRequest.getId();
        String name = chartQueryRequest.getName();
        String goal = chartQueryRequest.getGoal();
        String chartType = chartQueryRequest.getChartType();
        Long userId = chartQueryRequest.getUserId();
        String sortField = chartQueryRequest.getSortField();
        String sortOrder = chartQueryRequest.getSortOrder();
        String chartStatus = chartQueryRequest.getChartStatus();
        queryWrapper.eq(id != null && id > 0, "id", id);
        queryWrapper.like(StringUtils.isNotBlank(name), "name", name);
        queryWrapper.eq(StringUtils.isNotBlank(goal), "goal", goal);
        queryWrapper.eq(StringUtils.isNotBlank(chartType), "chartType", chartType);
        queryWrapper.eq(ObjectUtils.isNotEmpty(userId), "userId", userId);
        queryWrapper.eq(StringUtils.isNotBlank(chartStatus), "chartStatus", chartStatus);
        queryWrapper.eq("isDelete", false);
        queryWrapper.orderBy(SqlUtils.validSortField(sortField), sortOrder.equals(CommonConstant.SORT_ORDER_ASC),
                sortField);
        return queryWrapper;
    }


}
