package com.kaniblu.naver.api;

public class InvalidLoginException extends NaverException
{
    public InvalidLoginException(String msg)
    {
        super(msg);
    }
}
