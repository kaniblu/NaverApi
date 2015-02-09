package com.kaniblu.naver.api;

import org.json.JSONException;

import java.util.logging.Level;
import java.util.logging.Logger;

public class JSONArray
{
    private static final Logger logger = Logger.getLogger(JSONArray.class.getCanonicalName());

    private org.json.JSONArray mArray;

    public JSONArray(org.json.JSONArray array)
    {
        mArray = array;
    }

    public int length()
    {
        return mArray.length();
    }

    public JSONObject getJSONObject(int i)
    {
        try {
            return new JSONObject(mArray.getJSONObject(i));
        } catch (JSONException e) {
            logger.log(Level.SEVERE, "Unexpected json error.");
            return null;
        }
    }
}
