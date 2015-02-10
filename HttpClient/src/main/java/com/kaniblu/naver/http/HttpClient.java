package com.kaniblu.naver.http;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;

public class HttpClient
{
    private static Logger logger = Logger.getLogger(HttpClient.class.getCanonicalName());

    public enum Method
    {
        GET,
        POST
    }

    private static byte[] toByteArray(InputStream stream) throws IOException
    {
        ByteArrayOutputStream byteStream = new ByteArrayOutputStream();

        int read;
        byte[] buffer = new byte[4096];

        while ((read = stream.read(buffer, 0, buffer.length)) != -1)
            byteStream.write(buffer, 0, read);

        return byteStream.toByteArray();
    }

    public static HttpResult request(Method method, String url)
    {
        return _request(method, url, null, null);
    }

    public static HttpResult request(Method method, String url, HttpHeaderCollection headers)
    {
        return _request(method, url, headers, null);
    }

    public static HttpResult request(Method method, String url, HttpHeaderCollection headers, String content)
    {
        return _request(method, url, headers, content.getBytes());
    }

    public static HttpResult request(Method method, String url, HttpHeaderCollection headers, HttpForm formData)
    {
        byte[] content = null;

        if (formData != null) {
            String encoding = "UTF-8";
            String urlEncodedContent = formData.toURLEncodedString(encoding);

            try {
                content = urlEncodedContent.getBytes(encoding);
            } catch (UnsupportedEncodingException e) {
                logger.log(Level.SEVERE, "Unrecognized encoding type.");
                return null;
            }

            if (headers == null)
                headers = new HttpHeaderCollection();

            if (headers.containsKey("Content-Type"))
                headers.remove("Content-Type");

            headers.put("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8");
        }

        return _request(method, url, headers, content);
    }

    private static HttpResult _request(Method method, String url, HttpHeaderCollection headers, byte[] content)
    {
        URL urlObject = null;

        try {
            urlObject = new URL(url);
        } catch (MalformedURLException e) {
            logger.log(Level.WARNING, "Invalid url!", e);
            return null;
        }

        HttpURLConnection connection = null;

        try {
            connection = (HttpURLConnection)urlObject.openConnection();
        } catch (IOException e) {
            logger.log(Level.SEVERE, "Could not communicate with the server.", e);
            return null;
        }

        try {
            switch (method) {
                case POST:
                    connection.setRequestMethod("POST");
                    break;
                case GET:
                default:
                    connection.setRequestMethod("GET");
                    break;
            }
        } catch (ProtocolException e) {
            logger.log(Level.SEVERE, "Unexpected http request method error.", e);
            return null;
        }

        connection.setDoInput(true);
        connection.setDoOutput(true);

        if (headers != null)
            for (HttpHeader header : headers)
                connection.setRequestProperty(header.getKey(), header.getValue());

        if (content != null) {
            OutputStream stream = null;

            try {
                stream = connection.getOutputStream();
                stream.write(content);
                stream.flush();
                stream.close();
            } catch (IOException e) {
                logger.log(Level.SEVERE, "Failed to open output stream.");
                return null;
            }
        }

        InputStream inputStream = null;

        try {
            inputStream = connection.getInputStream();
        } catch (IOException e) {
            logger.log(Level.SEVERE, "Failed to retrieve response content.");
            return null;
        }

        HttpResult result = new HttpResult();
        result.url = url;

        try {
            result.statusCode = connection.getResponseCode();
        } catch (IOException e) {
            logger.log(Level.SEVERE, "Failed to get a response.");
            return null;
        }

        HttpHeaderCollection responseHeaders = new HttpHeaderCollection();
        responseHeaders.putAll(connection.getHeaderFields());
        result.headers = responseHeaders;

        byte[] byteArray = null;

        try {
            byteArray = toByteArray(inputStream);
        } catch (IOException e) {
            logger.log(Level.SEVERE, "Failed to read from inputstream.", e);
            return result;
        }

        result.content = byteArray;

        return result;
    }
}