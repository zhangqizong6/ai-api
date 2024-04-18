package cn.bugstack.chatgpt.data.types.model;

/**
 * @ClassName: Response
 * @author: zqz
 * @date: 2024/4/18 00:56
 */


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Response<T> implements Serializable {

    private String code;
    private String info;
    private T data;

}

