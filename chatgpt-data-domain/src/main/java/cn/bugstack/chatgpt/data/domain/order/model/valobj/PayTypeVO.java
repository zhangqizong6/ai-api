package cn.bugstack.chatgpt.data.domain.order.model.valobj;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @author Fuzhengwei bugstack.cn @小傅哥
 * @description 支付类型
 * @create 2023-09-29 08:29
 */
@Getter
@AllArgsConstructor
public enum PayTypeVO {

    WEIXIN_NATIVE(0, "微信Native支付"),
            ;

    private final Integer code;
    private final String desc;

    public static PayTypeVO get(Integer code){
        switch (code){
            case 0:
                return PayTypeVO.WEIXIN_NATIVE;
            default:
                return PayTypeVO.WEIXIN_NATIVE;
        }
    }

}
