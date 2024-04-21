package cn.bugstack.chatgpt.data.domain.openai.service.channel.impl;

import cn.bugstack.chatglm.model.*;
import cn.bugstack.chatglm.session.OpenAiSession;
import cn.bugstack.chatgpt.data.domain.openai.model.aggregates.ChatProcessAggregate;
import cn.bugstack.chatgpt.data.domain.openai.model.entity.MessageEntity;
import cn.bugstack.chatgpt.data.domain.openai.service.channel.OpenAiGroupService;
import cn.bugstack.chatgpt.data.types.enums.ChatGLMModel;
import cn.bugstack.chatgpt.data.types.exception.ChatGPTException;
import com.alibaba.fastjson.JSON;
import lombok.extern.slf4j.Slf4j;
import okhttp3.sse.EventSource;
import okhttp3.sse.EventSourceListener;
import org.jetbrains.annotations.Nullable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyEmitter;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * @ClassName: ChatGLMService
 * @author: zqz
 * @date: 2024/4/21 22:06
 */
@Slf4j
@Service
public class ChatGLMService implements OpenAiGroupService {

    @Autowired(required = false)
    protected OpenAiSession chatGlMOpenAiSession;

    @Override
    public void doMessageResponse(ChatProcessAggregate chatProcess, ResponseBodyEmitter emitter) throws Exception {
        if (null == chatGlMOpenAiSession) {
            emitter.send("ChatGLM 通道，模型调用未开启，可以选择其他模型对话！");
            return;
        }

// 1. 请求消息
        List<ChatCompletionRequest.Prompt> prompts = new ArrayList<>();

        List<MessageEntity> messages = chatProcess.getMessages();
        MessageEntity messageEntity = messages.remove(messages.size() - 1);

        for (MessageEntity message : messages) {
            String role = message.getRole();
            if (Objects.equals(role, Role.system.getCode())) {
                prompts.add(ChatCompletionRequest.Prompt.builder()
                        .role(Role.system.getCode())
                        .content(message.getContent())
                        .build());

                prompts.add(ChatCompletionRequest.Prompt.builder()
                        .role(Role.user.getCode())
                        .content("Okay")
                        .build());
            } else {
                prompts.add(ChatCompletionRequest.Prompt.builder()
                        .role(Role.user.getCode())
                        .content(message.getContent())
                        .build());

                prompts.add(ChatCompletionRequest.Prompt.builder()
                        .role(Role.user.getCode())
                        .content("Okay")
                        .build());
            }
        }

        prompts.add(ChatCompletionRequest.Prompt.builder()
                .role(messageEntity.getRole())
                .content(messageEntity.getContent())
                .build());

        // 2. 封装参数
        ChatCompletionRequest request = new ChatCompletionRequest();
        request.setModel(Model.valueOf(ChatGLMModel.get(chatProcess.getModel()).name())); // chatGLM_6b_SSE、chatglm_lite、chatglm_lite_32k、chatglm_std、chatglm_pro
        request.setPrompt(prompts);

        chatGlMOpenAiSession.completions(request, new EventSourceListener() {
            @Override
            public void onEvent(EventSource eventSource, @Nullable String id, @Nullable String type, String data) {
                ChatCompletionResponse response = JSON.parseObject(data, ChatCompletionResponse.class);

                // 发送信息
                if (EventType.add.getCode().equals(type)) {
                    try {
                        emitter.send(response.getData());
                    } catch (Exception e) {
                        throw new ChatGPTException(e.getMessage());
                    }
                }

                // type 消息类型，add 增量，finish 结束，error 错误，interrupted 中断
                if (EventType.finish.getCode().equals(type)) {
                    ChatCompletionResponse.Meta meta = JSON.parseObject(response.getMeta(), ChatCompletionResponse.Meta.class);
                    log.info("[输出结束] Tokens {}", JSON.toJSONString(meta));
                }
            }

            @Override
            public void onClosed(EventSource eventSource) {
                emitter.complete();
            }

        });
    }
}
