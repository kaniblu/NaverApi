package com.kaniblu.naver.api;

public class AuthRequiredException extends Exception
{
    public AuthRequiredException()
    {
        super();
    }

    public AuthRequiredException(String msg)
    {
        super(msg);
    }

    public AuthRequiredException(String msg, Throwable t)
    {
        super(msg, t);
    }
}
