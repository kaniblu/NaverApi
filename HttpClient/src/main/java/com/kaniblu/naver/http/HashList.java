package com.kaniblu.naver.http;

import java.util.*;

public class HashList<K, V> implements Iterable<V>
{
    protected Map<K, List<V>> mMap = new HashMap<K, List<V>>();
    protected List<V> mList = new ArrayList<V>();

    public HashList()
    {

    }

    public boolean containsKey(K key)
    {
        return mMap.containsKey(key);
    }

    public void put(K key, V value)
    {
        if (!containsKey(key))
            mMap.put(key, new ArrayList<V>());

        mMap.get(key).add(value);
        mList.add(value);
    }

    public void put(Map<K, List<V>> items)
    {
        for (Map.Entry<K, List<V>> entry : items.entrySet()) {
            K key = entry.getKey();
            for (V value : entry.getValue()) {
                put(key, value);
            }
        }
    }

    public List<V> get(K key)
    {
        return mMap.get(key);
    }

    public int size()
    {
        return mList.size();
    }

    public boolean remove(K key)
    {
        if (!containsKey(key))
            return false;

        List<V> removeList = get(key);
        for (V value : removeList)
            mList.remove(value);
        mMap.remove(key);

        return true;
    }

    @Override
    public Iterator<V> iterator()
    {
        return mList.iterator();
    }
}
