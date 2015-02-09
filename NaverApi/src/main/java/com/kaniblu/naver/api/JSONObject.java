package com.kaniblu.naver.api;

import org.json.JSONException;

import java.util.logging.Level;
import java.util.logging.Logger;

public class JSONObject
{
    private static final Logger logger = Logger.getLogger(JSONObject.class.getCanonicalName());

    private org.json.JSONObject mObject;

    public JSONObject(org.json.JSONObject obj)
    {
        mObject = obj;
    }

    public JSONObject(String str) throws JSONException
    {
        mObject = new org.json.JSONObject(str);
    }

    public JSONObject getJSONObject(String s)
    {
        try {
            return new JSONObject(mObject.getJSONObject(s));
        } catch (JSONException e) {
            logger.log(Level.SEVERE, "Unexpected json error.");
            return null;
        }
    }

    public JSONArray getJSONArray(String s)
    {
        try {
            return new JSONArray(mObject.getJSONArray(s));
        } catch (JSONException e) {
            logger.log(Level.SEVERE, "Unexpected json error.");
            return null;
        }
    }

    public boolean has(String key)
    {
        return mObject.has(key);
    }

    public String getString(String key) {
        try {
            return mObject.getString(key);
        } catch(JSONException e) {
            logger.log(Level.SEVERE, "Unexpected json error.");
            return null;
        }
    }

    public Long getLong(String key) {
        try {
            return mObject.getLong(key);
        } catch(JSONException e) {
            logger.log(Level.SEVERE, "Unexpected json error.");
            return null;
        }
    }

    public Integer getInt(String key) {
        try {
            return mObject.getInt(key);
        } catch(JSONException e) {
            logger.log(Level.SEVERE, "Unexpected json error.");
            return null;
        }
    }

    public Boolean getBoolean(String key) {
        try {
            return mObject.getBoolean(key);
        } catch(JSONException e) {
            logger.log(Level.SEVERE, "Unexpected json error.");
            return null;
        }
    }
}
