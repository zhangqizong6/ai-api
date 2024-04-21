package cn.bugstack.chatgpt.data.domain.openai.repository;

import cn.bugstack.chatgpt.data.domain.openai.model.entity.UserAccountQuotaEntity;

public interface IOpenAiRepository {

    int subAccountQuota(String openid);

    UserAccountQuotaEntity queryUserAccount(String openid);

}
