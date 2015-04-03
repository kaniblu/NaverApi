package com.kaniblu.naver.api;

import java.util.HashMap;
import java.util.Map;

/**
* Created by Kani on 4/3/2015.
*/
public enum DeviceType
{
    PC("pc"),
    MOBILE("mobile");

    private static final Map<String, DeviceType> REV_MAP = new HashMap<String, DeviceType>()
    {
        {
            put("pc", PC);
            put("mobile", MOBILE);
        }
    };

    private String mString;

    private DeviceType(String string)
    {
        mString = string;
    }

    public static DeviceType parse(String value)
    {
        if (REV_MAP.containsKey(value.toLowerCase()))
            return REV_MAP.get(value.toLowerCase());
        else
            return null;
    }

    @Override
    public String toString()
    {
        return mString;
    }
}
