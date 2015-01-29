package com.kaniblu.naver;

/**
 * Created by Kani on 1/27/2015.
 */
public class ServerException extends Exception
{
    public ServerException()
    {
        super("Server is unavailable right now.");
    }

    public ServerException(String msg)
    {
        super(msg);
    }
}
