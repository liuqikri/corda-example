package com.ehkd.blockchain.util;

import lombok.Data;

@Data
public class CordaResult<T> {

    private String message;

    private String code;

    private T data;

    public static <T> CordaResult<T> success(T data){
        CordaResult<T> CResult = new CordaResult<T>();
        CResult.setData(data);
        CResult.setCode(CordaResultCodeEnum.SUCCESS.getCode());
        CResult.setMessage(CordaResultCodeEnum.SUCCESS.getMessage());
        return CResult;
    }

    public static <T> CordaResult<T> successNoData(String code , String message){
        CordaResult<T> CResult = new CordaResult<>();
        CResult.setCode(code);
        CResult.setMessage(message);
        return CResult;
    }


    public static <T> CordaResult<T> error(String msg){
        CordaResult<T> CResult = new CordaResult<>();
        CResult.setCode(CordaResultCodeEnum.ERROR.getCode());
        CResult.setMessage(msg);
        return CResult;
    }

}
