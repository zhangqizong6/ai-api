package cn.bugstack.chatgpt.data.domain.weixin.service.validate;

import cn.bugstack.chatgpt.data.domain.weixin.service.IWeiXinValidateService;
import cn.bugstack.chatgpt.data.types.sdk.weixin.SignatureUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;


/**
 * @ClassName: WeiXinValidateService
 * @author: zqz
 * @date: 2024/4/17 18:17
 * 使用微信公众号提供的 SDK 即可验签。
 */
@Service
public class WeiXinValidateService implements IWeiXinValidateService {

    @Value("${wx.config.token}")
    private String token;

    @Override
    public boolean checkSign(String signature, String timestamp, String nonce) {
        return SignatureUtil.check(token, signature, timestamp, nonce);
    }
}
