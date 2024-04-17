package cn.bugstack.chatgpt.data.domain.auth.repository;

/**
 * @ClassName: IAuthRepository
 * @author: zqz
 * @date: 2024/4/17 17:41
 */

public interface IAuthRepository {

    String getCodeUserOpenId(String code);

    void removeCodeByOpenId(String code, String openId);

}
