package com.kaniblu.naver.api;

import java.util.HashMap;
import java.util.Map;

public enum UserType
{
    NAVER("naver"),
    FACEBOOK("facebook"),
    TWITTER("twitter");

    private static final Map<String, UserType> REV_MAP = new HashMap<String, UserType>()
    {
        {
            put("naver", NAVER);
            put("facebook", FACEBOOK);
            put("twitter", TWITTER);
        }
    };

    private String mString;

    private UserType(String string)
    {
        mString = string;
    }


    public static UserType parse(String value)
    {
        if (REV_MAP.containsKey(value))
            return REV_MAP.get(value);
        else
            return null;
    }

    @Override
    public String toString()
    {
        return mString;
    }
}
