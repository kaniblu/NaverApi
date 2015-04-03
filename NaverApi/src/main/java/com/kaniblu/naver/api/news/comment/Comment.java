package com.kaniblu.naver.api.news.comment;

import com.kaniblu.naver.api.*;
import com.kaniblu.naver.api.news.Article;
import com.kaniblu.naver.http.HttpForm;
import com.kaniblu.naver.http.HttpResult;
import org.joda.time.DateTime;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Comment
{
    private static final Logger logger = Logger.getLogger(Comment.class.getCanonicalName());


    public void setConnection(Connection connection)
    {
        mConnection = connection;
    }

    public void setId(int id)
    {
        mId = id;
    }

    public void setArticle(Article article)
    {
        mArticle = article;
    }

    protected Connection mConnection;
    protected int mId;
    protected Article mArticle;
    protected User mCommentor;
    protected int mNReplies;
    protected DateTime mTimestamp;
    protected String mContent;
    protected boolean mIsBest;
    protected int mNUp;
    protected int mNDown;
    protected DeviceType mDevice;

    public Comment(Connection connection)
    {
        mConnection = connection;
    }

    protected Comment()
    {

    }

    public Comment(Connection connection, Article article, int id)
    {
        this(connection);
        mArticle = article;
        mId = id;
    }

    public Comment(Connection connection, Article article, JSONObject object) throws ServerException, InternalException
    {
        this(connection);
        mArticle = article;
        loadFromJSON(object);
    }

    public Comment(Connection connection, JSONObject object) throws ServerException, InternalException
    {
        this(connection);

        mArticle = getArticleFromJSONObject(object);
        loadFromJSON(object);
    }

    protected Article getArticleFromJSONObject(JSONObject object)
    {
        String aid = null;
        String oid = null;
        if (object.getString("gno") != null) {
            String gno = object.getString("gno");
            Pattern pattern = Pattern.compile("^news(\\d+),(\\d+)$");
            Matcher match = pattern.matcher(gno);
            if (match.matches() && match.groupCount() == 2) {
                oid = match.group(1);
                aid = match.group(2);
            }
        } else if (object.getString("articleId") != null && object.getString("officeId") != null) {
            oid = object.getString("officeId");
            aid = object.getString("articleId");
        }

        if (aid == null || oid == null) {
            logger.log(Level.SEVERE, "Failed to find aid or oid in JSONObject.");
            return null;
        }


        Article article = new Article(mConnection, oid, aid);
        String title = object.getString("articleTitle");

        if (title != null)
            article.setTitle(title);

        return article;
    }

    private JSONObject requestReplies(int page, int pageSize, SortType sortType) throws InternalException, ServerException
    {
        HttpForm form = new HttpForm();
        form.put("commentNo", String.valueOf(mId));
        form.put("gno", mArticle.getGno());
        form.put("pageSize", String.valueOf(pageSize));
        form.put("page", String.valueOf(page));
        form.put("sort", sortType.toString());
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
        JSONObject object = requestReplies(1, 1, SortType.SCORE);

        if (!object.has("parentComment")) {
            logger.log(Level.SEVERE, "Unexpected absence of parentComment.");
            throw new ServerException();
        }

        object = object.getJSONObject("parentComment");

        loadFromJSON(object);
    }

    public List<Comment> getComments(int page, int pageSize, SortType sortType) throws InternalException, ServerException
    {
        List<Comment> commentList = new ArrayList<Comment>();
        JSONObject object = requestReplies(page, pageSize, sortType);

        if (!object.has("commentReplies")) {
            logger.log(Level.SEVERE, "Unexpected absence of commentReplies key.");
            throw new ServerException();
        }

        JSONArray commentsJson = object.getJSONArray("commentReplies");
        for (int i = 0; i < commentsJson.length(); ++i) {
            JSONObject commentObject = commentsJson.getJSONObject(i);
            Comment comment = new Comment(mConnection, mArticle, commentObject);
            commentList.add(comment);
        }

        return commentList;
    }

    public static List<Comment> getCurrentUserComments(Connection connection, int page, int pageSize, SortType sortType) throws InternalException, ServerException
    {
        List<Comment> comments = new ArrayList<Comment>();

        return comments;
    }

    private void loadFromJSON(JSONObject jsonObject) throws ServerException, InternalException
    {
        if (!jsonObject.has("lRegDate") && !jsonObject.has("sRegDate"))
            logger.log(Level.WARNING, "The comment json is missing timestamp.");
        else {
            if (jsonObject.has("lRegDate")) {
                long timestamp = jsonObject.getLong("lRegDate");
                mTimestamp = new DateTime(timestamp);
            }
            else if (jsonObject.has("sRegDate")) {
                String timeStr = jsonObject.getString("sRegDate");
                mTimestamp = Time.parse(timeStr).toJodaTime();
            }
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

        if (!jsonObject.has("userNickname") && !jsonObject.has("encodedUserId") && !jsonObject.has("userId"))
            logger.log(Level.WARNING, "The comment json is missing user info.");
        else {
            mCommentor = new User(mConnection, jsonObject);
        }

        if (!jsonObject.has("commentReplyNo") && !jsonObject.has("commentNo")) {
            logger.log(Level.SEVERE, "The comment json is missing comment identifier");
            throw new ServerException("Server returned malformed response.");
        } else {
            if (jsonObject.has("commentReplyNo"))
                mId = jsonObject.getInt("commentReplyNo");
            else if (jsonObject.has("commentNo"))
                mId = jsonObject.getInt("commentNo");
            else
                throw new InternalException();
        }

        if (!jsonObject.has("incomingType"))
            logger.log(Level.WARNING, "The comment json is missing incomingType.");
        else {
            String deviceType = jsonObject.getString("incomingType");
            DeviceType type = DeviceType.parse(deviceType);
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

    public Article getArticle()
    {
        return mArticle;
    }

    public User getCommentor()
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
