package cn.bugstack.chatgpt.data.domain.auth.service;

import cn.bugstack.chatgpt.data.domain.auth.model.entity.AuthStateEntity;

public interface IAuthService {

    /**
     * 登陆验证
     * @param code
     * @return token
     */
    AuthStateEntity doLogin(String code);

    boolean checkToken(String token);

    String openid(String token);
}
