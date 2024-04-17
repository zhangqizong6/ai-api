package cn.bugstack.chatgpt.data.domain.weixin.repository;

/**
 * @ClassName: IWeiXinRepository
 * @author: zqz
 * @date: 2024/4/17 18:21
 */

public interface IWeiXinRepository {

    String genCode(String openId);
}
