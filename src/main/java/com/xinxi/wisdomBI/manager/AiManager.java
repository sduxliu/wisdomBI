package com.xinxi.wisdomBI.manager;

import com.zhipu.oapi.ClientV4;
import com.zhipu.oapi.Constants;
import com.zhipu.oapi.service.v4.model.*;
import io.reactivex.Flowable;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

/**
 * 用于对接 AI 平台
 * @author 蒲月理想
 */
@Service
public class AiManager {
    @Resource
    private ClientV4 client;

    // 默认的是0.95，认为此时是稳定的
    // 较稳定的随机数
    public static final float STABLE_TEMPERATURE = 0.05f;
    // 不稳定的随机数
    public static final float UNSTABLE_TEMPERATURE = 0.99f;

    /**
     * 通用请求方法
     *
     * @param aiChatMessages AI聊天消息
     * @param stream 是否开启流式
     * @param temperature 随机性
     * @return AI响应信息
     */
    public String doRequest(List<ChatMessage> aiChatMessages, Boolean stream, Float temperature) {
        ChatCompletionRequest chatCompletionRequest = ChatCompletionRequest.builder()
                .model(Constants.ModelChatGLM4)
                .stream(stream)
                .invokeMethod(Constants.invokeMethod)
                .temperature(temperature)
                .messages(aiChatMessages)
                .build();
        try {
            ModelApiResponse invokeModelApiResp = client.invokeModelApi(chatCompletionRequest);
//            return invokeModelApiResp.getData().getChoices().get(0).getMessage().toString();
            return invokeModelApiResp.getData().getChoices().get(0).getMessage().getContent().toString();

        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }

    /**
     * 简化消息传递
     *
     * @param systemMessage 系统信息
     * @param userMessage 用户信息
     * @param stream 是否开启流式
     * @param temperature 随机性
     * @return AI响应信息
     */
    public String doRequest(String systemMessage, String userMessage, Boolean stream, Float temperature) {
        // 构造请求
        List<ChatMessage> aiChatMessages = new ArrayList<>();
        ChatMessage systemChatMessage = new ChatMessage(ChatMessageRole.SYSTEM.value(), systemMessage);
        ChatMessage userChatMessage = new ChatMessage(ChatMessageRole.USER.value(), userMessage);
        aiChatMessages.add(systemChatMessage);
        aiChatMessages.add(userChatMessage);
        return doRequest(aiChatMessages, stream, temperature);
    }

    /**
     * 同步请求,自定义回答随机性
     *
     * @param systemMessage 系统信息
     * @param userMessage 用户信息
     * @param temperature 随机性
     * @return AI响应信息
     */
    public String doSyncRequest(String systemMessage, String userMessage, Float temperature) {
        return doRequest(systemMessage, userMessage, Boolean.FALSE, temperature);
    }

    /**
     * 同步请求（答案较稳定）
     *
     * @param systemMessage 系统信息
     * @param userMessage 用户信息
     * @return AI响应信息
     */
    public String doSyncStableRequest(String systemMessage, String userMessage) {
        return doRequest(systemMessage, userMessage, Boolean.FALSE, STABLE_TEMPERATURE);
    }

    /**
     * 同步请求（答案较随机）
     *
     * @param systemMessage 系统信息
     * @param userMessage 用户信息
     * @return AI响应信息
     */
    public String doSyncUnStableRequest(String systemMessage, String userMessage) {
        return doRequest(systemMessage, userMessage, Boolean.FALSE, UNSTABLE_TEMPERATURE);
    }

    /**
     * 通用流式请求
     *
     * @param aiChatMessages AI聊天消息
     * @param temperature    随机性
     * @return AI响应信息(流式)
     */
    public Flowable<ModelData> doStreamRequest(List<ChatMessage> aiChatMessages, Float temperature) {
        ChatCompletionRequest chatCompletionRequest = ChatCompletionRequest.builder()
                .model(Constants.ModelChatGLM4)
                .stream(Boolean.TRUE)
                .invokeMethod(Constants.invokeMethod)
                .temperature(temperature)
                .messages(aiChatMessages)
                .build();
        ModelApiResponse modelApiResponse = client.invokeModelApi(chatCompletionRequest);
        return modelApiResponse.getFlowable();
    }

    /**
     * 通用流式请求(简化消息传递)
     *
     * @param systemMessage 系统信息
     * @param userMessage 用户信息
     * @param temperature 随机性
     * @return AI响应信息(流式)
     */
    public Flowable<ModelData> doStreamRequest(String systemMessage, String userMessage, Float temperature) {
        List<ChatMessage> aiChatMessages = new ArrayList<>();
        ChatMessage systemChatMessage = new ChatMessage(ChatMessageRole.SYSTEM.value(), systemMessage);
        ChatMessage userChatMessage = new ChatMessage(ChatMessageRole.USER.value(), userMessage);
        aiChatMessages.add(systemChatMessage);
        aiChatMessages.add(userChatMessage);
        return doStreamRequest(aiChatMessages, temperature);
    }

//    @Resource
//    private YuCongMingClient yuCongMingClient;
//
//    /**
//     * AI 对话
//     *
//     * @param modelId
//     * @param message
//     * @return
//     */
//    public String doChat(long modelId, String message) {
//        DevChatRequest devChatRequest = new DevChatRequest();
//        devChatRequest.setModelId(modelId);
//        devChatRequest.setMessage(message);
//        BaseResponse<DevChatResponse> response = yuCongMingClient.doChat(devChatRequest);
//        if (response == null) {
//            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "AI 响应错误");
//        }
//        return response.getData().getContent();
//    }
}
