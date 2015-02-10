package com.kaniblu.naver.http;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.logging.Level;
import java.util.logging.Logger;

public class HttpForm extends HashList<String, StringPair>
{
    private static final Logger logger = Logger.getLogger(HttpForm.class.getCanonicalName());

    public String toURLEncodedString(String encoding)
    {
        String content = "";

        try {
            for (StringPair pair : this)
                content += URLEncoder.encode(pair.getFirst(), encoding) + "=" + URLEncoder.encode(pair.getSecond(), encoding);
        } catch (UnsupportedEncodingException e) {
            logger.log(Level.SEVERE, "Unrecognized encoding.", e);
            return null;
        }

        return content;
    }

    public void put(String key, String value)
    {
        put(key, new StringPair(key, value));
    }

    public String toURLEncodedString()
    {
        return toURLEncodedString("UTF-8");
    }
}
