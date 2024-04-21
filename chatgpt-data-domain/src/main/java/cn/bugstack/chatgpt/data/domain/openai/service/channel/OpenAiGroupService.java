package cn.bugstack.chatgpt.data.domain.openai.service.channel;

import cn.bugstack.chatgpt.data.domain.openai.model.aggregates.ChatProcessAggregate;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyEmitter;

/**
 * 服务组
 */
public interface OpenAiGroupService {

    void doMessageResponse(ChatProcessAggregate chatProcess, ResponseBodyEmitter emitter) throws Exception;

}
