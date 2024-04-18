package cn.bugstack.chatgpt.data.domain.openai.service.rule.impl;

import cn.bugstack.chatgpt.data.domain.openai.annotation.LogicStrategy;
import cn.bugstack.chatgpt.data.domain.openai.model.aggregates.ChatProcessAggregate;
import cn.bugstack.chatgpt.data.domain.openai.model.entity.MessageEntity;
import cn.bugstack.chatgpt.data.domain.openai.model.entity.RuleLogicEntity;
import cn.bugstack.chatgpt.data.domain.openai.model.entity.UserAccountQuotaEntity;
import cn.bugstack.chatgpt.data.domain.openai.model.valobj.LogicCheckTypeVO;
import cn.bugstack.chatgpt.data.domain.openai.service.rule.ILogicFilter;
import cn.bugstack.chatgpt.data.domain.openai.service.rule.factory.DefaultLogicFactory;
import com.github.houbb.sensitive.word.bs.SensitiveWordBs;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @ClassName: SensitiveWordFilter
 * @author: zqz
 * @date: 2024/4/18 21:00
 * <p>
 * 敏感次过滤
 * 敏感词的服务对象，在 chatgpt-data-app 下的 SensitiveWordConfig 中定义 @Bean 对象提供使用
 */
@Slf4j
@Service
@LogicStrategy(logicMode = DefaultLogicFactory.LogicModel.SENSITIVE_WORD)
public class SensitiveWordFilter implements ILogicFilter<UserAccountQuotaEntity> {

    /**
     * 倒入开源敏感词包
     */
    @Resource
    private SensitiveWordBs words;

    @Value("${app.config.white-list}")
    private String whiteListStr;

    @Override
    public RuleLogicEntity<ChatProcessAggregate> filter(ChatProcessAggregate chatProcess, UserAccountQuotaEntity data) throws Exception {
        // 白名单用户不做敏感词处理
        if (chatProcess.isWhiteList(whiteListStr)) {
            return RuleLogicEntity.<ChatProcessAggregate>builder()
                    .type(LogicCheckTypeVO.SUCCESS).data(chatProcess).build();
        }

        ChatProcessAggregate newChatProcessAggregate = new ChatProcessAggregate();
        newChatProcessAggregate.setOpenid(chatProcess.getOpenid());
        newChatProcessAggregate.setModel(chatProcess.getModel());

        List<MessageEntity> newMessages = chatProcess.getMessages().stream().map(message -> {
            String content = message.getContent();
            String replace = words.replace(content);
            return MessageEntity.builder()
                    .role(message.getRole())
                    .name(message.getName())
                    .content(replace)
                    .build();
        }).collect(Collectors.toList());

        newChatProcessAggregate.setMessages(newMessages);

        return RuleLogicEntity.<ChatProcessAggregate>builder()
                .type(LogicCheckTypeVO.SUCCESS)
                .data(newChatProcessAggregate)
                .build();
    }
}
