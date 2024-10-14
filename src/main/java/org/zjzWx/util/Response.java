package org.zjzWx.util;

import lombok.Data;

import java.io.Serializable;

@Data
public class Response<T> implements Serializable {

    private static final long serialVersionUID = -6630747483482976634L;

    private Integer code;
    private String msg;
    private T data;


    public Response(Integer code, String msg, T data) {
        this.code = code;
        this.msg = msg;
        this.data = data;
    }



    public static <T> Response<T> ok(T data) {
        return new Response<>(200,"请求成功",data);
    }
    public static <T> Response<T> no() {
        return new Response<>(404,"暂无数据",null);
    }

    public static <T> Response<T> no(Integer code, String message) {
        return new Response<>(code,message,null);
    }
    public static <T> Response<T> no(T data) {
        return new Response<>(404,"操作失败",data);
    }


}
