package com.kaniblu.naver.api;

import com.kaniblu.naver.http.*;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.json.JSONException;

import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Connection
{
    protected static final Logger logger = Logger.getLogger(Connection.class.getCanonicalName());

    protected static class Cookie
    {
        public static final DateTimeFormatter EXPIRE_FORMAT = DateTimeFormat.forPattern("EEE, dd-MM-yyyy HH:mm:ss zzz");

        public String key;
        public String value;
        public DateTime expireDate;

        public Cookie(String key, String value, DateTime expire)
        {
            this.key = key;
            this.value = value;
            this.expireDate = expire;
        }

        public Cookie(String key, String value)
        {
            this(key, value, DateTime.now().plusYears(100));
        }

        public boolean isExpired()
        {
            return expireDate.isBefore(DateTime.now());
        }
    }

    protected static class CookieCollection
    {
        protected HashMap<String, Cookie> mData;
        protected List<Cookie> mList;

        public CookieCollection()
        {
            mData = new HashMap<String, Cookie>();
            mList = new ArrayList<Cookie>();
        }

        public void add(Cookie cookie)
        {
            if (mData.containsKey(cookie.key)) {
                mList.remove(mData.get(cookie.key));
                mData.remove(cookie.key);
            }

            mData.put(cookie.key, cookie);
        }

        public boolean contains(String key)
        {
            return mData.containsKey(key);
        }

        public boolean isExpired(String key)
        {
            if (mData.containsKey(key))
                return mData.get(key).expireDate.isBefore(DateTime.now());
            else
                return true;
        }

        public void remove(String key)
        {
            mList.remove(mData.get(key));
            mData.remove(key);
        }

        public String get(String key)
        {
            return mData.get(key).value;
        }

        public DateTime getExpireDate(String key)
        {
            return mData.get(key).expireDate;
        }

        public Set<Cookie> cookieSet()
        {
            return new Set<Cookie>()
            {
                @Override
                public int size()
                {
                    return 0;
                }

                @Override
                public boolean isEmpty()
                {
                    return false;
                }

                @Override
                public boolean contains(Object o)
                {
                    return false;
                }

                @Override
                public Iterator<Cookie> iterator()
                {
                    return new Iterator<Cookie>()
                    {
                        protected Iterator<String> mKeySet = mData.keySet().iterator();

                        @Override
                        public boolean hasNext()
                        {
                            return mKeySet.hasNext();
                        }

                        @Override
                        public Cookie next()
                        {
                            return mData.get(mKeySet.next());
                        }

			@Override
			public void remove()
			{
			    return;
			}
                    };
                }

                @Override
                public Object[] toArray()
                {
                    return new Object[0];
                }

                @Override
                public <T> T[] toArray(T[] a)
                {
                    return null;
                }

                @Override
                public boolean add(Cookie cookie)
                {
                    return false;
                }

                @Override
                public boolean remove(Object o)
                {
                    return false;
                }

                @Override
                public boolean containsAll(Collection<?> c)
                {
                    return false;
                }

                @Override
                public boolean addAll(Collection<? extends Cookie> c)
                {
                    return false;
                }

                @Override
                public boolean retainAll(Collection<?> c)
                {
                    return false;
                }

                @Override
                public boolean removeAll(Collection<?> c)
                {
                    return false;
                }

                @Override
                public void clear()
                {

                }
            };
        }

        public int size()
        {
            return mData.size();
        }
    }
    protected static final Pattern ERR_PATTERN = Pattern.compile("<div class=\"error\" id=\"err_common\">.*?<p>([^<]*?)</p>.*?</div>", Pattern.DOTALL);

    protected KeySet mKeySet;
    protected CookieCollection mCookies = new CookieCollection();

    public Connection()
    {
    }

    protected static class KeySet
    {
        public String sessionKey;
        public String keyName;
        public String eValue;
        public String nValue;

        public KeySet()
        {

        }

        @Override
        public String toString()
        {
            return sessionKey + "," + keyName + "," + eValue + "," + nValue;
        }
    }

    protected void requestKeys() throws InternalException
    {
        HttpResult result = HttpClient.request(HttpClient.Method.POST, "https://nid.naver.com/login/ext/keys.nhn");

        if (result.getStatusCode() / 100 != 2 || result.getContent() == null) {
            logger.log(Level.SEVERE, "Key request returned abnormal status code or empty content.");
            throw new InternalException();
        }

        String[] tokens = result.getContentAsString().split(",");

        if (tokens.length != 4) {
            logger.log(Level.SEVERE, "Key request failed to retrieve four keys.");
            throw new InternalException();
        }

        KeySet keySet = new KeySet();
        keySet.sessionKey = tokens[0].trim();
        keySet.keyName = tokens[1].trim();
        keySet.eValue = tokens[2].trim();
        keySet.nValue = tokens[3].trim();

        mKeySet = keySet;
    }

    protected String generateCookieHeader()
    {
        String s = "";

        for (Cookie cookie : mCookies.cookieSet())
            if (!cookie.isExpired())
                s += cookie.key + "=" + cookie.value + "; ";

        return s.length() > 0 ? s.substring(0, s.length() - 2) : s;
    }

    protected HttpHeaderCollection generateDefaultRequestHeader()
    {
        HttpHeaderCollection header = new HttpHeaderCollection();
        header.put("Accept", "*/*");
        header.put("Accept-Language", "en-GB,en;q=0.8,en-US;q=0.6,ko;q=0.4,pt;q=0.2,zh-CN;q=0.2,zh;q=0.2,zh-TW;q=0.2,ja;q=0.2");
        header.put("Cookie", generateCookieHeader());
        header.put("User-Agent", "Mozilla/5.0 (Windows NT 6.3; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/39.0.2171.99 Safari/537.36");
        header.put("Connection", "keep-alive");
        return header;
    }

    public HttpResult requestGet(String url, HttpHeaderCollection header, HttpForm content) throws ServerException
    {
        return request(HttpClient.Method.GET, url, header, content);
    }

    public HttpResult requestPost(String url, HttpHeaderCollection header, HttpForm content) throws ServerException
    {
        return request(HttpClient.Method.POST, url, header, content);
    }

    protected void storeCookie(HttpResult result)
    {
        if (result != null && result.getHeaders().containsKey("set-cookie")) {
            for (HttpHeader header : result.getHeaders().get("set-cookie")) {
                String headerValue = header.getValue();
                String[] cookieKVP = headerValue.split(";");

                if (cookieKVP.length < 1)
                    continue;

                String kvp = cookieKVP[0];
                HashMap<String, String> params = new HashMap<String, String>();

                for (int i = 1; i < cookieKVP.length; ++i) {
                    String[] pair = cookieKVP[i].split("=");

                    if (pair.length != 2)
                        continue;

                    params.put(pair[0].toLowerCase().trim(), pair[1].trim());
                }

                String[] pair = kvp.split("=");

                if (pair.length != 2) {
                    logger.log(Level.INFO, "malformed cookie: " + headerValue);
                    continue;
                }

                String key = pair[0].trim();
                String value = pair[1].trim();

                if (value.toLowerCase().equals("expired")) {
                    if (mCookies.contains(key))
                        mCookies.remove(key);
                    continue;
                }

                DateTime expireDate = DateTime.now().plusYears(1000);

                if (params.containsKey("expire"))
                    expireDate = Cookie.EXPIRE_FORMAT.parseDateTime(params.get("expire"));

                mCookies.add(new Cookie(key, value, expireDate));
            }
        }
    }

    protected HttpResult request(HttpClient.Method method, String url, HttpHeaderCollection header, HttpForm content) throws ServerException
    {
        HttpHeaderCollection basicHeader = generateDefaultRequestHeader();

        if (header == null)
            header = basicHeader;
        else {
            for (HttpHeader h : basicHeader)
                if (!header.containsKey(h.getKey()))
                    header.put(h.getKey(), h.getValue());
        }

        HttpResult result = HttpClient.request(method, url, header, content);
        storeCookie(result);

        if (result != null) {
            switch (result.getStatusCode() / 100) {
                case 2:
                    logger.log(Level.INFO, "The return status is Ok: " + result.getStatusCode());
                    break;
                case 3:
                    logger.log(Level.INFO, "The return status is Ok but with caution: " + result.getStatusCode());
                    break;
                case 4:
                    logger.log(Level.INFO, "The return status states that there was a client error: " + result.getStatusCode());
                    break;
                case 5:
                    logger.log(Level.INFO, "The return status states that there was a server-side error: " + result.getStatusCode());
                    break;
            }
        }

        if (result == null)
            throw new ServerException();

        return result;
    }

    public JSONObject requestJsonPost(String url, HttpHeaderCollection header, HttpForm content) throws InternalException, ServerException, JSONErrorException
    {
        return requestJson(HttpClient.Method.POST, url, header, content);
    }

    public JSONObject requestJsonGet(String url, HttpHeaderCollection header) throws InternalException, ServerException, JSONErrorException
    {
        return requestJson(HttpClient.Method.GET, url, header, null);
    }

    protected JSONObject requestJson(HttpClient.Method method, String url, HttpHeaderCollection header, HttpForm content) throws InternalException, ServerException, JSONErrorException
    {
        HttpResult result = request(method, url, header, content);

        if (!result.isStatusOk())
            throw new ServerException();

        if (!result.hasContent()) {
            logger.log(Level.SEVERE, "The server didn't return any json content");
            throw new ServerException();
        }

        String jsonContent = result.getContentAsString();
        JSONObject object = null;

        try {
            object = new JSONObject(jsonContent);
        } catch (JSONException e) {
            logger.log(Level.SEVERE, "Failed to parse json content.");
            throw new InternalException();
        }

        if (!object.has("message")) {
            logger.log(Level.SEVERE, "Unexpected absence of 'message' key'");
            throw new InternalException();
        }

        object = object.getJSONObject("message");

        if (object.has("result")) {
            return object.getJSONObject("result");
        } else if (object.has("error")) {
            if (!object.has("code")) {
                logger.log(Level.SEVERE, "Unexpected absence of 'code' key in 'error'");
                throw new InternalException();
            }

            if (!object.has("msg")) {
                logger.log(Level.SEVERE, "Unexpected absence of 'msg' key in 'error'");
                throw new InternalException();
            }

            String code = object.getString("code");
            String msg = object.getString("msg");

            logger.log(Level.INFO, "Server returned an error json message: " + code + "/" + msg);
            throw new JSONErrorException(code, msg);
        } else {
            logger.log(Level.SEVERE, "Unrecognized json format.");
            throw new ServerException();
        }
    }

    protected static String extractErrorMsg(String page)
    {
        Matcher m = ERR_PATTERN.matcher(page);
        if (m.find())
            return m.group(1);
        else
            return null;
    }
}
