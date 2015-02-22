package com.kaniblu.naver.api;

import java.util.List;

public class NewsCommentor
{
    public enum Type
    {
        NAVER,
        FACEBOOK,
        TWITTER,
    }

    protected String mHashed;
    protected String mUsername;
    protected Type mType;

    public String getHashed()
    {
        return mHashed;
    }

    public String getUsername()
    {
        return mUsername;
    }

    public Type getType()
    {
        return mType;
    }

    public NewsCommentor()
    {

    }

    public NewsCommentor(String username, String hashed, Type type)
    {
        mHashed = hashed;
        mUsername = username;
        mType = type;
    }
}
