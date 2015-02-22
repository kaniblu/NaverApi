package com.kaniblu.naver.api;

public class NaverException extends Exception
{
    public NaverException()
    {
        super();
    }

    public NaverException(String msg)
    {
        super(msg);
    }

    public NaverException(String msg, Throwable t)
    {
        super(msg, t);
    }
}
