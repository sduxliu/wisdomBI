package com.xinxi.wisdomBI.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.xinxi.wisdomBI.model.dto.chart.GenChartByAiRequest;
import com.xinxi.wisdomBI.model.entity.Chart;
import com.xinxi.wisdomBI.model.entity.User;
import com.xinxi.wisdomBI.model.vo.BiResponse;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;

/**
 *
 * @author 蒲月理想
 */
public interface ChartService extends IService<Chart> {
    // 生成图表——同步方式
    BiResponse genChartByAi(GenChartByAiRequest genChartByAiRequest, MultipartFile multipartFile, HttpServletRequest request);

    // 生成图表——异步方式——线程池
    BiResponse genChartByAiAsync(GenChartByAiRequest genChartByAiRequest, MultipartFile multipartFile, HttpServletRequest request);
    // 生成图表——异步方式——消息队列
    BiResponse genChartByAiAsyncByMQ(GenChartByAiRequest genChartByAiRequest, MultipartFile multipartFile, HttpServletRequest request);;
    // 处理图表更新失败的情况
    void handleChartUpdateError(long chartId, String execMessage);
    // 发送给AI的信息生成
    StringBuilder getMessage(String goal, String chartType, String csvData);
    // 检查生成图表的请求是否合法
     void checkRequest(String name, String goal, MultipartFile multipartFile);
     // 保存异步请求的原始图表信息
     Chart saveChart(GenChartByAiRequest genChartByAiRequest, User loginUser, String csvData);

     // 异步（消息队列和多线程）AI生成图表并更新图表信息
    boolean aiGenChartAndUpdateOldChartInfo(StringBuilder userInput, Chart chart);

    // 重新生成
    Boolean regenerateChart(Long id, Long loginUserId);
}
