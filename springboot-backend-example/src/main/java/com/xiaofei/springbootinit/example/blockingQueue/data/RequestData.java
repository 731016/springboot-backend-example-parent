package com.xiaofei.springbootinit.example.blockingQueue.data;

import lombok.Data;

import java.io.Serializable;

/**
 * @author tuaofei
 * @description 接收数据
 * @date 2025/3/3
 */
@Data
public class RequestData<T> implements Serializable {
    private T data;
    private String type;
}
