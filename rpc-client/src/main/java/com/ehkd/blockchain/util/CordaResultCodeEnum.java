package com.ehkd.blockchain.util;

public enum CordaResultCodeEnum {
    SUCCESS("CO0000","success"),
    ERROR("CO0001","fail");

    private final String code;
    private final String message;

    CordaResultCodeEnum(String code,String name){
        this.code = code;
        this.message = name;
    }

    public String getCode(){
        return code;
    }

    public String getMessage(){
        return message;
    }
}
