package com.kaniblu.naver;

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
