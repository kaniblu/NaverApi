package com.kaniblu.naver.http;

import java.io.UnsupportedEncodingException;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

public class HttpResult
{
    private static final Logger logger = Logger.getLogger(HttpResult.class.getCanonicalName());

    public String url;
    public int statusCode;
    public Map<String, List<String>> headers;
    public byte[] content;

    public HttpResult(String url, int statusCode, Map<String, List<String>> headers, byte[] content)
    {
        url = url;
        statusCode = statusCode;
        headers = headers;
        content = content;
    }

    public String tryGetEncoding()
    {
        if (headers.containsKey("content-encoding"))
            return headers.get("content-encoding").get(0);
        else if (headers.containsKey("content-type")) {
            for (String type : headers.get("content-type")) {
                String[] tokens = type.split(";");
                if (tokens.length > 1) {
                    tokens = tokens[1].split("=");
                    if (tokens[0].trim().toLowerCase().equals("charset"))
                        return tokens[1].trim().toLowerCase();
                }
            }
        }

        return null;
    }

    public String getContentAsString()
    {
        if (content == null)
            return null;

        String encoding = tryGetEncoding();
        if (encoding == null)
            encoding = "utf-8";

        String contentStr = null;
        try {
            contentStr = new String(content, encoding);
        } catch (UnsupportedEncodingException e) {
            logger.log(Level.SEVERE, "Content uses unrecognized encoding.", e);
            return null;
        }

        return contentStr;
    }

    public boolean hasContent()
    {
        return content != null && content.length > 0;
    }

    public HttpResult()
    {

    }

    public boolean isStatusOk()
    {
        return statusCode / 100 == 2;
    }
}
