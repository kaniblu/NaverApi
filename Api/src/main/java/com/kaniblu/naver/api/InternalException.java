package com.kaniblu.naver.api;

public class InternalException extends Exception
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
