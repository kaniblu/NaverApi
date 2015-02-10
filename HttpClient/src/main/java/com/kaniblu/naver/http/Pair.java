package com.kaniblu.naver.http;

public class Pair<T1, T2>
{
    protected T1 mFirst;
    protected T2 mSecond;

    public Pair(T1 first, T2 second)
    {
        mFirst = first;
        mSecond = second;
    }

    public T1 getFirst()
    {
        return mFirst;
    }

    public T2 getSecond()
    {
        return mSecond;
    }

    public void setFirst(T1 first)
    {
        mFirst = first;
    }

    public void setSecond(T2 second)
    {
        mSecond = second;
    }
}
