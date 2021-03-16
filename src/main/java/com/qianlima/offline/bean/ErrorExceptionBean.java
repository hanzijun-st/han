package com.qianlima.offline.bean;

public class ErrorExceptionBean extends Exception{

    private int code;
    public ErrorExceptionBean(String message,int code){
        super(message);
        this.code=code;
    }

    public int getCode(){
        return this.code;
    }

}