package cn.bugstack.chatgpt.data.domain.openai.service.rule;

import cn.bugstack.chatgpt.data.domain.openai.model.aggregates.ChatProcessAggregate;
import cn.bugstack.chatgpt.data.domain.openai.model.entity.RuleLogicEntity;

/**
 * 实现 AccessLimitFilter、SensitiveWordFilter 两个实现类。
 */
public interface ILogicFilter<T> {

    RuleLogicEntity<ChatProcessAggregate> filter(ChatProcessAggregate chatProcess,T data) throws Exception;

}
