package cn.bugstack.chatgpt.data.domain.openai.service.rule.impl;

import cn.bugstack.chatgpt.data.domain.openai.annotation.LogicStrategy;
import cn.bugstack.chatgpt.data.domain.openai.model.aggregates.ChatProcessAggregate;
import cn.bugstack.chatgpt.data.domain.openai.model.entity.RuleLogicEntity;
import cn.bugstack.chatgpt.data.domain.openai.model.entity.UserAccountQuotaEntity;
import cn.bugstack.chatgpt.data.domain.openai.model.valobj.LogicCheckTypeVO;
import cn.bugstack.chatgpt.data.domain.openai.service.rule.ILogicFilter;
import cn.bugstack.chatgpt.data.domain.openai.service.rule.factory.DefaultLogicFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * @ClassName: ModelTypeFilter
 * @author: zqz
 * @date: 2024/4/19 18:15
 *
 * 可用模型
 */
@Slf4j
@Component
@LogicStrategy(logicMode = DefaultLogicFactory.LogicModel.MODEL_TYPE)
public class ModelTypeFilter implements ILogicFilter<UserAccountQuotaEntity> {


    @Override
    public RuleLogicEntity<ChatProcessAggregate> filter(ChatProcessAggregate chatProcess, UserAccountQuotaEntity data) throws Exception {
        List<String> allowModelTypeList = data.getAllowModelTypeList();
        String modelType = chatProcess.getModel();
        // 2. 模型校验通过
        if (allowModelTypeList.contains(modelType)) {
            return RuleLogicEntity.<ChatProcessAggregate>builder()
                    .type(LogicCheckTypeVO.SUCCESS)
                    .data(chatProcess)
                    .build();
        }

        // 3. 模型校验拦截
        return RuleLogicEntity.<ChatProcessAggregate>builder()
                .type(LogicCheckTypeVO.REFUSE)
                .info("当前账户不支持使用 " + modelType + " 模型！可以联系客服升级账户。")
                .data(chatProcess)
                .build();
    }
}
