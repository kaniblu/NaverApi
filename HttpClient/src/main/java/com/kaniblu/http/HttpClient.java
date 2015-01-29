package com.kaniblu.http;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.*;
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

    public static HttpResult request(Method method, String url, Map<String, String> headers)
    {
        return _request(method, url, headers, null);
    }

    public static HttpResult request(Method method, String url, Map<String, String> headers, String content)
    {
        return _request(method, url, headers, new StringEntity(content, "text/plain"));
    }

    public static HttpResult request(Method method, String url, Map<String, String> headers, Map<String, String> formData)
    {
        StringEntity formEntity = null;

        if (formData != null)
            formEntity = constructFormEntity(formData);

        return _request(method, url, headers, formEntity);
    }

    private static StringEntity constructFormEntity(Map<String, String> formData)
    {
        List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();

        for (Map.Entry<String, String> entry : formData.entrySet())
            nameValuePairs.add(new BasicNameValuePair(entry.getKey(), entry.getValue()));

        try {
            return new UrlEncodedFormEntity(nameValuePairs);
        } catch (UnsupportedEncodingException e) {
            logger.log(Level.SEVERE, "Form data contains unsupported encoding.", e);
            return null;
        }
    }

    private static HashMap<String, List<String>> getHeaders(HttpResponse response)
    {
        HashMap<String, List<String>> headerMap = new HashMap<String, List<String>>();
        for (Header header : response.getAllHeaders()) {
            String key = header.getName().toLowerCase();
            if (!headerMap.containsKey(key))
                headerMap.put(key, new ArrayList<String>());
            headerMap.get(key).add(header.getValue());
        }

        return headerMap;
    }

    private static HttpResult _request(Method method, String url, Map<String, String> headers, StringEntity content)
    {
        org.apache.http.impl.client.CloseableHttpClient client = HttpClients.createDefault();
        HttpResponse response = null;

        try {
            if (method == Method.GET) {
                HttpGet get = new HttpGet(url);

                if (headers != null)
                    for (Map.Entry<String, String> entry : headers.entrySet())
                        get.setHeader(entry.getKey(), entry.getValue());

                response = client.execute(get);
            } else if (method == Method.POST) {
                HttpPost post = new HttpPost(url);

                if (headers != null)
                    for (Map.Entry<String, String> entry : headers.entrySet())
                        post.setHeader(entry.getKey(), entry.getValue());
                if (content != null)
                    post.setEntity(content);

                response = client.execute(post);
            } else {
                logger.log(Level.SEVERE, "Critical Error: unknown method.");
                return null;
            }
        } catch (ClientProtocolException e) {
            logger.log(Level.SEVERE, "Protocol error occurred during request.", e);
            return null;
        } catch (IOException e) {
            logger.log(Level.SEVERE, "IO error occurred during request.", e);
            return null;
        }

        HttpResult result = new HttpResult();
        result.url = url;

        Integer statusCode = response.getStatusLine().getStatusCode();
        result.statusCode = statusCode;

        HashMap<String, List<String>> responseHeaders = getHeaders(response);
        result.headers = responseHeaders;

        HttpEntity entity = response.getEntity();

        if (entity == null) {
            logger.log(Level.INFO, "Response entity is empty.");
            return result;
        }

        InputStream contentStream = null;

        try {
            contentStream = entity.getContent();
        } catch (IOException e) {
            logger.log(Level.SEVERE, "Failed to get content from entity.", e);
            return result;
        }

        byte[] byteArray = null;

        try {
            byteArray = toByteArray(contentStream);
        } catch (IOException e) {
            logger.log(Level.SEVERE, "Failed to read from inputstream.", e);
            return result;
        }

        try {
            EntityUtils.consume(entity);
        } catch (IOException e) {
            logger.log(Level.INFO, "Failed to close the entity.", e);
        }

        byte[] contentBytes = byteArray;
        result.content = contentBytes;

        return result;
    }
}