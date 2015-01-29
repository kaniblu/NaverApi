package com.kaniblu.naver;

import com.kaniblu.http.HttpForm;
import com.kaniblu.http.HttpHeaders;
import com.kaniblu.http.HttpResult;
import org.joda.time.DateTime;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

public class NewsComment
{
    private static final Logger logger = Logger.getLogger(NewsComment.class.getCanonicalName());

    public enum DeviceType
    {
        PC,
        MOBILE,
        TABLET
    }

    public enum SortType
    {
        SCORE,
        DATE_ASC,
        DATE_DESC,
        NREPLY_DESC,
    }

    private Connection mConnection;
    private int mId;
    private NewsArticle mArticle;
    private NewsCommentor mCommentor;
    private int mNReplies;
    private DateTime mTimestamp;
    private String mContent;
    private boolean mIsBest;
    private int mNUp;
    private int mNDown;
    private DeviceType mDevice;

    public NewsComment(Connection connection)
    {
        mConnection = connection;
    }

    public NewsComment(Connection connection, NewsArticle article, int id)
    {
        this(connection);
        mArticle = article;
        mId = id;
    }

    public NewsComment(Connection connection, NewsArticle article, JSONObject object) throws ServerException
    {
        this(connection);
        mArticle = article;
        loadFromJSON(object);
    }

    private JSONObject requestReplies(int page, int pageSize, NewsComment.SortType sortType) throws InternalException, ServerException
    {
        HttpForm form = new HttpForm();
        form.put("commentNo", String.valueOf(mId));
        form.put("gno", mArticle.getGno());
        form.put("pageSize", "0");
        form.put("serviceId", "news");

        JSONObject object = null;

        try {
            object = mConnection.requestJsonPost("http://comment.news.naver.com/api/reply/list.json", null, form);
        } catch (JSONErrorException e) {
            logger.log(Level.SEVERE, "Unexpected json error: " + e.getMessage());
            throw new InternalException();
        }

        return object;
    }

    public void retrieve() throws InternalException, ServerException
    {
        JSONObject object = requestReplies(0, 0, SortType.SCORE);

        if (!object.has("parentComment")) {
            logger.log(Level.SEVERE, "Unexpected absence of parentComment.");
            throw new ServerException();
        }

        object = object.getJSONObject("parentComment");

        loadFromJSON(object);
    }

    public List<NewsComment> getComments(int page, int pageSize, NewsComment.SortType sortType) throws InternalException, ServerException
    {
        List<NewsComment> commentList = new ArrayList<NewsComment>();
        JSONObject object = requestReplies(page, pageSize, sortType);

        if (!object.has("commentReplies")) {
            logger.log(Level.SEVERE, "Unexpected absence of commentReplies key.");
            throw new ServerException();
        }

        JSONArray commentsJson = object.getJSONArray("commentReplies");
        for (int i = 0; i < commentsJson.length(); ++i) {
            JSONObject commentObject = commentsJson.getJSONObject(i);
            NewsComment comment = new NewsComment(mConnection, mArticle, commentObject);
            commentList.add(comment);
        }

        return commentList;
    }

    private void loadFromJSON(JSONObject jsonObject) throws ServerException
    {
        if (!jsonObject.has("lRegDate"))
            logger.log(Level.WARNING, "The comment json is missing timestamp.");
        else {
            long timestamp = jsonObject.getLong("lRegDate");
            mTimestamp = new DateTime(timestamp);
        }

        if (!jsonObject.has("content"))
            logger.log(Level.WARNING, "The comment json is missing content.");
        else {
            String content = jsonObject.getString("content");
            mContent = content;
        }

        if (!jsonObject.has("goodCount"))
            logger.log(Level.WARNING, "The comment json is missing goodCount.");
        else
            mNUp = jsonObject.getInt("goodCount");

        if (!jsonObject.has("badCount"))
            logger.log(Level.WARNING, "The comment json is missing badCount.");
        else
            mNDown = jsonObject.getInt("badCount");

        if (!jsonObject.has("isBest"))
            logger.log(Level.WARNING, "The comment json is missing isBest.");
        else
            mIsBest = jsonObject.getBoolean("isBest");

        if (!jsonObject.has("userNickname") || !jsonObject.has("encodedUserId") || !jsonObject.has("snsType"))
            logger.log(Level.WARNING, "The comment json is missing user info.");
        else {
            String username = jsonObject.getString("userNickname");
            String encoded = jsonObject.getString("encodedUserId");
            String type = jsonObject.getString("snsType");
            NewsCommentor.Type tType = null;

            if (type.equals("naver"))
                tType = NewsCommentor.Type.NAVER;
            else if (type.equals("twitter"))
                tType = NewsCommentor.Type.TWITTER;
            else if (type.equals("facebook"))
                tType = NewsCommentor.Type.FACEBOOK;
            else
                logger.log(Level.WARNING, "SnsType is not recognized.");

            NewsCommentor commentor = new NewsCommentor(username, encoded, tType);
            mCommentor = commentor;
        }

        if (!jsonObject.has("commentReplyNo")) {
            logger.log(Level.SEVERE, "The comment json is missing commentReplyNo");
            throw new ServerException("Server returned malformed response.");
        } else
            mId = jsonObject.getInt("commentReplyNo");

        if (!jsonObject.has("incomingType"))
            logger.log(Level.WARNING, "The comment json is missing incomingType.");
        else {
            String deviceType = jsonObject.getString("incomingType");
            DeviceType type = null;

            if (deviceType.equals("pc"))
                type = DeviceType.PC;
            else if (deviceType.equals("mobile"))
                type = DeviceType.MOBILE;
            else
                logger.log(Level.WARNING, "incomingType is not recognized: " + deviceType);

            mDevice = type;
        }

        if (!jsonObject.has("replyCount"))
            logger.log(Level.WARNING, "The comment json is missing replyCount");
        else
            mNReplies = jsonObject.getInt("replyCount");
    }

    public void delete() throws InternalException, ServerException
    {
        HttpForm form = new HttpForm();
        form.put("commentNo", "1052557");
        form.put("gno", "news001,007379616");
        form.put("serviceId", "news");
        form.put("listType", "undefined");

        HttpResult result = mConnection.requestPost("http://comment.news.naver.com/api/comment/delete.json", null, form);
    }
    public Connection getConnection()
    {
        return mConnection;
    }

    public int getId()
    {
        return mId;
    }

    public NewsArticle getArticle()
    {
        return mArticle;
    }

    public NewsCommentor getCommentor()
    {
        return mCommentor;
    }

    public int getNReplies()
    {
        return mNReplies;
    }

    public DateTime getTimestamp()
    {
        return mTimestamp;
    }

    public String getContent()
    {
        return mContent;
    }

    public boolean isBest()
    {
        return mIsBest;
    }

    public int getNUp()
    {
        return mNUp;
    }

    public int getNDown()
    {
        return mNDown;
    }

    public DeviceType getDevice()
    {
        return mDevice;
    }
}
