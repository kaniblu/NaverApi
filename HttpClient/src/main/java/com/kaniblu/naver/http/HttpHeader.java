package com.kaniblu.naver.http;

public class HttpHeader
{
    private String mKey;
    private String mValue;

    public HttpHeader(String key, String value)
    {
        mKey = key;
        mValue = value;
    }

    public String getKey()
    {
        return mKey;
    }

    public String getValue()
    {
        return mValue;
    }
}
