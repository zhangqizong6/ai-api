package cn.bugstack.chatgpt.data.domain.weixin.service;

import cn.bugstack.chatgpt.data.domain.weixin.model.entity.UserBehaviorMessageEntity;

/**
 * 回复验证码
 */
public interface IWeiXinBehaviorService {

    String acceptUserBehavior(UserBehaviorMessageEntity userBehaviorMessageEntity);

}
