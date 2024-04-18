package cn.bugstack.chatgpt.data.domain.openai.service;

import cn.bugstack.chatgpt.data.domain.openai.model.aggregates.ChatProcessAggregate;
import cn.bugstack.chatgpt.data.domain.openai.model.entity.RuleLogicEntity;
import cn.bugstack.chatgpt.data.domain.openai.model.entity.UserAccountQuotaEntity;
import cn.bugstack.chatgpt.data.domain.openai.model.valobj.LogicCheckTypeVO;
import cn.bugstack.chatgpt.data.domain.openai.service.rule.factory.DefaultLogicFactory;
import cn.bugstack.chatgpt.data.types.common.Constants;
import cn.bugstack.chatgpt.data.types.exception.ChatGPTException;
import cn.bugstack.chatgpt.session.OpenAiSession;
import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyEmitter;

import javax.annotation.Resource;
import java.io.IOException;

/**
 * @author Fuzhengwei bugstack.cn @小傅哥
 * @description
 * @create 2023-07-22 21:12
 */
@Slf4j
public abstract class AbstractChatService implements IChatService {

    @Resource
    protected OpenAiSession openAiSession;

    @Resource
    protected cn.bugstack.chatglm.session.OpenAiSession openAiGLMSession;

    @Override
    public ResponseBodyEmitter completions(ChatProcessAggregate chatProcess) throws Exception {
        // 1. 校验权限
//        if (!"b8b6".equals(chatProcess.getToken())) {
//            throw new ChatGPTException(Constants.ResponseCode.TOKEN_ERROR.getCode(), Constants.ResponseCode.TOKEN_ERROR.getInfo());
//        }

        // 2. 请求应答
        ResponseBodyEmitter emitter = new ResponseBodyEmitter(3 * 60 * 1000L);
        emitter.onCompletion(() -> {
            log.info("流式问答请求完成，使用模型：{}", chatProcess.getModel());
        });

        emitter.onError(throwable -> log.error("流式问答请求疫情，使用模型：{}", chatProcess.getModel(), throwable));


        //3.规则过滤
        //doCheckLogic由子类实现
        RuleLogicEntity<ChatProcessAggregate> ruleLogicEntity = this.doCheckLogic(chatProcess, null,
                DefaultLogicFactory.LogicModel.ACCESS_LIMIT.getCode(),
                DefaultLogicFactory.LogicModel.SENSITIVE_WORD.getCode());

        if (!LogicCheckTypeVO.SUCCESS.equals(ruleLogicEntity.getType())) {
            log.info("用户【{}】不放行，状态为【{}】",chatProcess.getOpenid(),ruleLogicEntity);
            emitter.send(ruleLogicEntity.getInfo());
            emitter.complete();
            return emitter;
        }
        // 3. 应答处理
        try {
            this.doMessageResponse(chatProcess, emitter);
        } catch (Exception e) {
            throw new ChatGPTException(Constants.ResponseCode.UN_ERROR.getCode(), Constants.ResponseCode.UN_ERROR.getInfo());
        }

        // 4. 返回结果
        return emitter;
    }

    @Override
    public ResponseBodyEmitter completionsGLM(ChatProcessAggregate chatProcess) throws Exception {
//        //1.校验权限
//        if (!"b8b6".equals(chatProcess.getToken())) {
//            throw new ChatGPTException(Constants.ResponseCode.TOKEN_ERROR.getCode(), Constants.ResponseCode.TOKEN_ERROR.getInfo());
//        }

        //2.请求应答
        //ResponseBodyEmitter是一种用于将响应数据发送给客户端的异步模式，可以在Spring MVC的控制器方法中使用。
        ResponseBodyEmitter emitter = new ResponseBodyEmitter(3 * 60 * 1000L);
        emitter.onCompletion(() -> {
            log.info("流式问答请求完成，使用模型：{}", chatProcess.getModel());
        });
        emitter.onError(throwable -> log.error("流式问答请求，使用模型：{}", chatProcess.getModel(), throwable));

        UserAccountQuotaEntity userAccountQuotaEntity = new UserAccountQuotaEntity();
        userAccountQuotaEntity.setOpenid("123456");

        //3.规则过滤
        //doCheckLogic由子类实现
        RuleLogicEntity<ChatProcessAggregate> ruleLogicEntity = this.doCheckLogic(chatProcess, userAccountQuotaEntity,
                DefaultLogicFactory.LogicModel.ACCESS_LIMIT.getCode(),
                DefaultLogicFactory.LogicModel.SENSITIVE_WORD.getCode());

        if (!LogicCheckTypeVO.SUCCESS.equals(ruleLogicEntity.getType())) {
            emitter.send(ruleLogicEntity.getInfo());
            emitter.complete();
            return emitter;
        }

        //4.应答处理
        try {
            this.doGLMMessageResponse(chatProcess, emitter);
        } catch (Exception e) {
            throw new ChatGPTException(Constants.ResponseCode.UN_ERROR.getCode(), Constants.ResponseCode.UN_ERROR.getInfo());
        }

        // 4. 返回结果
        return emitter;
    }

    protected abstract void doMessageResponse(ChatProcessAggregate chatProcess, ResponseBodyEmitter responseBodyEmitter) throws JsonProcessingException;

    protected abstract void doGLMMessageResponse(ChatProcessAggregate chatProcess, ResponseBodyEmitter responseBodyEmitter);

    protected abstract RuleLogicEntity<ChatProcessAggregate> doCheckLogic(ChatProcessAggregate chatProcess, UserAccountQuotaEntity userAccountQuotaEntity, String... logics) throws Exception;
}
