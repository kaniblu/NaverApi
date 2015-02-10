package com.kaniblu.naver.http;

import java.io.UnsupportedEncodingException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class HttpResult
{
    private static final Logger logger = Logger.getLogger(HttpResult.class.getCanonicalName());

    protected String url;
    protected int statusCode;
    protected HttpHeaderCollection headers;
    protected byte[] content;

    public String getUrl()
    {
        return url;
    }

    public int getStatusCode()
    {
        return statusCode;
    }

    public HttpHeaderCollection getHeaders()
    {
        return headers;
    }

    public byte[] getContent()
    {
        return content;
    }

    public HttpResult(String url, int statusCode, HttpHeaderCollection headers, byte[] content)
    {
        this.url = url;
        this.statusCode = statusCode;
        this.headers = headers;
        this.content = content;
    }

    public String tryGetEncoding()
    {
        if (headers.containsKey("content-encoding"))
            return headers.get("content-encoding").get(0).getValue();
        else if (headers.containsKey("content-type")) {
            for (HttpHeader type : headers.get("content-type")) {
                String[] tokens = type.getValue().split(";");
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
