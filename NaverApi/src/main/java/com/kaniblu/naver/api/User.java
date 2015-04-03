package com.kaniblu.naver.api;

import com.kaniblu.naver.api.news.comment.Comment;
import com.kaniblu.naver.api.news.comment.SortType;
import com.kaniblu.naver.http.HttpClient;
import com.kaniblu.naver.http.HttpForm;
import com.kaniblu.naver.http.HttpHeaderCollection;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class User
{
    private static Logger logger = Logger.getLogger(User.class.getCanonicalName());

    protected Connection mConnection;
    protected String mEncodedUsername;
    protected String mUsername;
    protected String mNickname;
    protected boolean mNameReal = true;
    protected boolean mCommentWritable = true;
    protected UserType mType;
    protected boolean mLoggedIn = false;

    public User(Connection connection)
    {
        mConnection = connection;
    }

    public User(Connection connection, JSONObject object)
    {
        mConnection = connection;
    }

    public boolean isLoggedIn()
    {
        return mLoggedIn;
    }

    public void setLoggedIn(boolean loggedIn)
    {
        mLoggedIn = loggedIn;
    }

    public String getNickname()
    {
        return mNickname;
    }

    public void setNickname(String nickname)
    {
        mNickname = nickname;
    }

    public boolean isNameReal()
    {
        return mNameReal;
    }

    public void setNameReal(boolean isRealName)
    {
        this.mNameReal = isRealName;
    }

    public boolean isCommentWritable()
    {
        return mCommentWritable;
    }

    public void setCommentWritable(boolean commentWritable)
    {
        this.mCommentWritable = commentWritable;
    }

    public String getEncodedUsername()
    {
        return mEncodedUsername;
    }

    public String getUsername()
    {
        return mUsername;
    }

    public UserType getType()
    {
        return mType;
    }

    public List<Comment> getComments(int page, int pageSize, SortType sortType) throws NaverException
    {
        HttpForm form = new HttpForm();
        form.put("page", String.valueOf(page));
        form.put("pageSize", String.valueOf(pageSize));
        form.put("sort", sortType.toString());

        JSONObject object = null;
        try {
            object = mConnection.requestJsonPost("http://comment.news.naver.com/api/usercontent/comment.json", null, form);
        } catch (JSONErrorException e) {
            logger.log(Level.SEVERE, "Unexpected json error.", e);
            throw new InternalException();
        }

        JSONArray commentsJson = object.getJSONArray("userContents");

        if (commentsJson == null) {
            logger.log(Level.SEVERE, "JSONObject doesn't contain userContents JSONArray.");
            throw new ServerException();
        }

        List<Comment> commentList = new ArrayList<Comment>();

        for (int i = 0; i < commentsJson.length(); ++i) {
            JSONObject commentObject = commentsJson.getJSONObject(i);
            Comment comment = new Comment(mConnection, commentObject);
            commentList.add(comment);
        }

        return commentList;
    }

    public void loadFromJSON(JSONObject object)
    {
        mEncodedUsername = object.getString("encodedUserId");
        mUsername = object.getString("userId");
        mNickname = object.getString("userNickname");

        if (object.has("isRealname"))
            mNameReal = object.getBoolean("isRealname");

        if (object.has("isWritable"))
            mCommentWritable = object.getBoolean("isWritable");

        if (object.has("maskUserId"))
            mUsername = object.getString("maskUserId");

        if (object.has("snsType") && object.getString("snsType").length() > 0)
            mType = UserType.parse(object.getString("snsType"));

        if (object.has("isLogin"))
            mLoggedIn = object.getBoolean("isLogin");
    }

    public void retrieve() throws InternalException, ServerException
    {
        HttpHeaderCollection header = new HttpHeaderCollection();

        JSONObject object = null;
        try {
            object = mConnection.requestJson(HttpClient.Method.POST, "http://comment.news.naver.com/api/authCheck.json", header, null);
        } catch (JSONErrorException e) {
            logger.log(Level.WARNING, "AuthCheck returned malformed json.", e);
            throw new ServerException(e.getMsg());
        }

        loadFromJSON(object);
    }
}
