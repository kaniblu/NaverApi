package com.kaniblu.naver.api;

public class JSONErrorException extends Exception
{
    private String mCode;
    private String mMsg;

    public String getCode()
    {
        return mCode;
    }

    public String getMsg()
    {
        return mMsg;
    }

    public JSONErrorException(String code, String msg)
    {
        super("(" + code + ") " + msg);
    }
}
