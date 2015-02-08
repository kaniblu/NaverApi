package com.kaniblu.naver.api;

import java.util.List;

/**
 * Created by Kani on 1/27/2015.
 */
public class NewsCommentor
{
    public enum Type
    {
        NAVER,
        FACEBOOK,
        TWITTER,
    }

    private String mHashed;
    private String mUsername;
    private Type mType;

    public NewsCommentor(String username, String hashed, Type type)
    {
        mHashed = hashed;
        mUsername = username;
        mType = type;
    }
}
