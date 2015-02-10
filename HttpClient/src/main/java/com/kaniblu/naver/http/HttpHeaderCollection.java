package com.kaniblu.naver.http;

import java.util.*;

public class HttpHeaderCollection extends HashList<String, HttpHeader>
{
    public void put(String key, String value)
    {
        put(key, new HttpHeader(key, value));
    }

    public void put(HttpHeader header)
    {
        put(header.getKey(), header);
    }

    public void putAll(Map<String, List<String>> headers)
    {
        for (Map.Entry<String, List<String>> entry : headers.entrySet()) {
            String key = entry.getKey();
            for (String value : entry.getValue()) {
                put(key, value);
            }
        }
    }

    public List<HttpHeader> get(String key)
    {
        return mMap.get(key);
    }

    public int size()
    {
        return mList.size();
    }

    public boolean remove(HttpHeader header)
    {
        if (!containsKey(header.getKey()))
            return false;

        boolean result = get(header.getKey()).remove(header);
        boolean result2 = mList.remove(header);

        return result && result2;
    }

    @Override
    public Iterator<HttpHeader> iterator()
    {
        return mList.iterator();
    }
}

	