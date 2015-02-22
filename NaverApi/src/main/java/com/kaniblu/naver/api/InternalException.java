package com.kaniblu.naver.api;

public class InternalException extends NaverException
{
    public InternalException()
    {
        super("An internal error occurred.");
    }

    public InternalException(String msg)
    {
        super(msg);
    }
}
