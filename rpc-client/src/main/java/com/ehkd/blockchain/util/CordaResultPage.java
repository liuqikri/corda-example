package com.ehkd.blockchain.util;

import lombok.Data;

@Data
public class CordaResultPage<T> {

    private String message;

    private String code;
    private Long totalCount;
    private  Integer pageNumber;
    private  Integer pageSize;
    private T data;


    public static <T> CordaResultPage<T> success (Long totalCount, Integer pageNumber, Integer pageSize, T data){
        CordaResultPage<T> CResultPage = new CordaResultPage<T>();
        CResultPage.setPageNumber(pageNumber);
        CResultPage.setPageSize(pageSize);
        CResultPage.setTotalCount(totalCount);
        CResultPage.setData(data);
        CResultPage.setCode(CordaResultCodeEnum.SUCCESS.getCode());
        return CResultPage;
    }


    public static <T> CordaResultPage<T> successNoData(String code , String message){
        CordaResultPage<T> CResultPage = new CordaResultPage<>();
        CResultPage.setCode(code);
        CResultPage.setMessage(message);
        return CResultPage;
    }


    public static <T> CordaResultPage<T> error(String msg){
        CordaResultPage<T> CResultPage = new CordaResultPage<>();
        CResultPage.setCode(CordaResultCodeEnum.ERROR.getCode());
        CResultPage.setMessage(msg);
        return CResultPage;
    }

    public static <T> CordaResultPage<T> error(String code, String message){
        CordaResultPage<T> CResultPage = new CordaResultPage<>();
        CResultPage.setCode(code);
        CResultPage.setMessage(message);
        return CResultPage;
    }
}
