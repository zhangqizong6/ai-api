package cn.bugstack.chatgpt.data.domain.weixin.service;

/**
 * 验证签名
 */
public interface IWeiXinValidateService {

    boolean checkSign(String signature, String timestamp, String nonce);

}
