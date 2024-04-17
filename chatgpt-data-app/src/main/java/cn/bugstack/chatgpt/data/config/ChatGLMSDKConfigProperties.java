package cn.bugstack.chatgpt.data.config;

/**
 * @ClassName: ChatGLMSDKConfigProperties
 * @author: zqz
 * @date: 2024/4/15 22:00
 */

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "chatglm.sdk.config", ignoreInvalidFields = true)
public class ChatGLMSDKConfigProperties {

    private String apiHost;

    private String apiKey;

}
