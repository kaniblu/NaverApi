package com.kaniblu.naver;

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
