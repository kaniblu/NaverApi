package com.kaniblu.naver;

import com.kaniblu.http.HttpForm;
import com.kaniblu.http.HttpHeaders;
import com.kaniblu.http.HttpResult;
import com.sun.corba.se.spi.activation.Server;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

public class NewsArticle
{
    private static final Logger logger = Logger.getLogger(NewsArticle.class.getCanonicalName());

    private Connection mConnection;
    private String mOid;
    private String mAid;
    private String mContent;
    private DateTime mTimestamp;
    private int mNComment;

    public Connection getConnection()
    {
        return mConnection;
    }

    public String getOid()
    {
        return mOid;
    }

    public String getAid()
    {
        return mAid;
    }

    public DateTime getTimestamp()
    {
        return mTimestamp;
    }

    public int getNComment()
    {
        return mNComment;
    }

    public NewsArticle(Connection connection, String oid, String aid)
    {
        mConnection = connection;
        mOid = oid;
        mAid = aid;
    }

    public void retrieveContent() throws ServerException, InternalException
    {
        HttpResult result = mConnection.requestGet("http://news.naver.com/main/read.nhn?oid=" + mOid + "&aid=" + mAid, null, null);

        if (result == null || !result.isStatusOk() || !result.hasContent()) {
            logger.log(Level.SEVERE, "Could not contact the server, or the server returned an error.");
            throw new ServerException();
        }

        Document doc = Jsoup.parse(result.getContentAsString());
        Elements divs = doc.select("div[id=articleBodyContents]");

        if (divs.size() < 1) {
            logger.log(Level.SEVERE, "Unable to parse news article page.");
            throw new ServerException();
        }

        mContent = divs.get(0).text().trim();

        divs = doc.select("div[class=sponsor]");

        if (divs.size() < 1) {
            logger.log(Level.SEVERE, "Unable to parse news article.");
            throw new ServerException();
        }

        Elements timestamps = divs.select("span[class=t11]");

        if (timestamps.size() < 1) {
            logger.log(Level.SEVERE, "Unable to parse news article timestamp.");
            throw new ServerException();
        }

        DateTimeFormatter dateTimeFormatter = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm");
        try {
            mTimestamp = dateTimeFormatter.parseDateTime(timestamps.get(0).text());
        } catch (IllegalArgumentException e) {
            logger.log(Level.SEVERE, "Datetime format is invalid.");
            throw new ServerException();
        }

        Elements replyCounts = divs.select("span[class=lo_txt]");

        if (replyCounts.size() < 1) {
            logger.log(Level.SEVERE, "Unable to parse news article replyCounts.");
            throw new ServerException();
        }

        mNComment = Integer.parseInt(replyCounts.get(0).text());
    }

    public NewsComment writeComment(String content) throws ServerException, InternalException
    {
        String gno = "news" + mOid + "," + mAid;

        HttpHeaders header = new HttpHeaders();
        header.put("charset", "utf-8");
        header.put("Host", "comment.news.naver.com");
        header.put("Origin", "http://comment.news.naver.com");
        header.put("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8");

        HttpForm formContent = new HttpForm();
        formContent.put("content", content);
        formContent.put("gno", gno);
        formContent.put("serviceId", "news");
        formContent.put("incomingType", "pc");

        JSONObject object = null;

        try {
            object = mConnection.requestJsonPost("http://comment.news.naver.com/api/comment/write.json", header, formContent);
        } catch (JSONErrorException e) {
            logger.log(Level.SEVERE, "Unexpected json error.");
            throw new ServerException();
        }

        if (!object.has("commentReplies")) {
            logger.log(Level.SEVERE, "Unexpected json format.");
            throw new ServerException();
        }

        JSONArray array = object.getJSONArray("commentReplies");

        if (array.length() < 1) {
            logger.log(Level.SEVERE, "The number of comments posted is not 1.");
            throw new InternalException();
        }

        NewsComment comment = new NewsComment(mConnection, this, array.getJSONObject(0));

        return comment;
    }

    public List<NewsComment> getComments(int page, int pageSize, NewsComment.SortType sortType) throws InternalException, ServerException
    {
        String gno = "news" + mOid + "," + mAid;

        HttpForm formContent = new HttpForm();
        formContent.put("gno", gno);
        formContent.put("serviceId", "news");
        formContent.put("pageSize", String.valueOf(pageSize));
        formContent.put("page", String.valueOf(page));

        String sortBy = null;

        switch (sortType) {
            case SCORE:
                sortBy = "likability";
                break;
            case DATE_ASC:
                sortBy = "oldest";
                break;
            case DATE_DESC:
                sortBy = "newest";
                break;
            case NREPLY_DESC:
                sortBy = "replyCount";
                break;
        }

        formContent.put("sort", sortBy);

        JSONObject object = null;

        try {
            object = mConnection.requestJsonPost("http://comment.news.naver.com/api/comment/list.json", null, formContent);
        } catch (JSONErrorException e) {
            logger.log(Level.SEVERE, "Unexpected json error.");
            throw new InternalException();
        }

        List<NewsComment> commentList = new ArrayList<NewsComment>();

        if (!object.has("commentReplies")) {
            logger.log(Level.SEVERE, "Unexpected absence of 'message.result.commentReplies'");
            throw new ServerException("The server returned malformed json.");
        }

        JSONArray commentsJson = object.getJSONArray("commentReplies");
        for (int i = 0; i < commentsJson.length(); ++i) {
            JSONObject commentObject = commentsJson.getJSONObject(i);
            NewsComment comment = new NewsComment(mConnection, this, commentObject);
            commentList.add(comment);
        }

        return commentList;
    }

    public String getGno()
    {
        return "news" + mOid + "," + mAid;
    }

    public String getContent()
    {
        return mContent;
    }
}
