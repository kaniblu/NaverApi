package com.kaniblu.naver.api;

public class JSONErrorException extends Exception
{
    private String mCode;
    private String mMsg;

    public JSONErrorException(String code, String msg)
    {
        super("(" + code + ") " + msg);
    }
}
