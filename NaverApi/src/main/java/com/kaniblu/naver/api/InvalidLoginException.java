package com.kaniblu.naver.api;

public class InvalidLoginException extends Exception
{
    public InvalidLoginException(String msg)
    {
        super(msg);
    }
}
