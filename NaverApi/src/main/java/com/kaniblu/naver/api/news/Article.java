package com.kaniblu.naver.api.news;

import com.kaniblu.naver.api.*;
import com.kaniblu.naver.api.news.comment.Comment;
import com.kaniblu.naver.api.news.comment.SortType;
import com.kaniblu.naver.http.HttpForm;
import com.kaniblu.naver.http.HttpHeaderCollection;
import com.kaniblu.naver.http.HttpResult;
import com.sun.corba.se.spi.activation.Server;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Article
{
    private static final DateTimeFormatter DAILYRANK_TIMESTAMP_FORMAT = DateTimeFormat.forPattern("yyyy-MM-dd");
    private static final Logger logger = Logger.getLogger(Article.class.getCanonicalName());

    public void setConnection(Connection connection)
    {
        mConnection = connection;
    }

    public void setOid(String oid)
    {
        mOid = oid;
    }

    public void setAid(String aid)
    {
        mAid = aid;
    }

    public boolean hasImages()
    {
        return mHasImages;
    }

    public void setHasImages(boolean hasImages)
    {
        mHasImages = hasImages;
    }

    protected boolean mHasImages = false;
    protected List<String> mHighlights = new ArrayList<String>();

    public List<String> getHighlights()
    {
        return mHighlights;
    }

    protected Connection mConnection;
    protected String mOid;
    protected String mAid;
    protected ContentElements mContent;
    protected DateTime mTimestamp;
    protected int mCommentSize;
    protected String mTitle;
    protected String mPress;
    protected String mCategory;
    protected int mLikes;
    protected boolean mLiked;

    public int getLikes()
    {
        return mLikes;
    }

    public void setLikes(int likes)
    {
        mLikes = likes;
    }

    public String getCategory()
    {
        return mCategory;
    }

    public void setCategory(String category)
    {
        mCategory = category;
    }

    public String getPress()
    {
        return mPress;
    }

    public void setPress(String press)
    {
        mPress = press;
    }

    public void setTitle(String title)
    {
        mTitle = title;
    }

    public void setCommentSize(int NComment)
    {
        mCommentSize = NComment;
    }

    public void setTimestamp(DateTime timestamp)
    {
        mTimestamp = timestamp;
    }

    public String getTitle()
    {
        return mTitle;
    }

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

    public int getCommentSize()
    {
        return mCommentSize;
    }

    public Article(Connection connection, String oid, String aid)
    {
        mConnection = connection;
        mOid = oid;
        mAid = aid;
    }

    public Article()
    {

    }

    public static List<Article> retrieveWeeklyTopNews(Connection connection, Category category, DateTime date) throws ServerException, InternalException
    {
        return retrieveRankedNews(connection, "popularWeek", category, date);
    }

    public static List<Article> retrieveDailyTopNews(Connection connection, Category category, DateTime date) throws ServerException, InternalException
    {
        return retrieveRankedNews(connection, "popularDay", category, date);
    }

    public static List<Article> retrieveWeeklyMostCommentedNews(Connection connection, Category category, DateTime date) throws ServerException, InternalException
    {
        return retrieveRankedNews(connection, "memoWeek", category, date);
    }

    protected static String toUrlDateString(DateTime date)
    {
        return String.format("%04d%02d%02d", date.getYear(), date.getMonthOfYear(), date.getDayOfMonth());
    }

    protected static List<Article> retrieveRankedNews(Connection connection, String page, Category category, DateTime date) throws ServerException, InternalException
    {
        if (date == null)
            date = DateTime.now();

        if (category == null)
            category = Category.ALL;

        if (page == null)
            page = "popularDay";

        String url = String.format("http://news.naver.com/main/ranking/%s.nhn?sectionId=%s&date=%s", page, category.toString(), toUrlDateString(date));

        HttpResult result = connection.requestGet(url, null, null);

        if (result == null || !result.isStatusOk() || !result.hasContent()) {
            logger.log(Level.SEVERE, "Could not contact the server, or the server returned an error.");
            throw new ServerException();
        }

        Document doc = Jsoup.parse(result.getContentAsString());

        Elements rank = doc.select("div.ranking_top3 > ol > li > dl");
        Elements rankOthers = doc.select("ol.all_ranking > li > dl > dt");

        if (rank == null || rankOthers == null || rank.isEmpty() || rankOthers.isEmpty()) {
            logger.log(Level.SEVERE, "Unable to parse daily ranked news article page.");
            throw new ServerException();
        }

        rank.addAll(rankOthers);

        List<Article> rankedNewsList = new LinkedList<Article>();

        Pattern aidPattern = Pattern.compile("aid=([^&]+)");
        Pattern oidPattern = Pattern.compile("oid=([^&]+)");

        for (Element e : rank) {
            boolean hasImages = false;
            String press = null;
            DateTime timestamp = null;

            Elements links = e.select("a");

            if (links.size() <= 0) {
                logger.info("No link found in news item.");
                continue;
            }

            Elements imgIcons = e.select("img");

            if (imgIcons.size() > 0 && imgIcons.get(0).attr("title").equals("\ud3ec\ud1a0"))
                hasImages = true;

            Elements iconSpans = e.select("span[class=ico]");

            if (iconSpans.size() > 0) {
                Element iconSpan = iconSpans.get(0);
                Element pressSpan = iconSpan.parent();
                press = pressSpan.text().trim();
            }

            Elements dateSpans = e.select("span[class=num]");

            if (dateSpans.size() > 0) {
                Element dateSpan = dateSpans.get(0);
                timestamp = DAILYRANK_TIMESTAMP_FORMAT.parseDateTime(dateSpan.text().trim());
            }

            Element link = links.get(0);

            String href = link.attr("href");
            String title = link.text().trim();

            String aid = "";
            String oid = "";

            Matcher matcher = aidPattern.matcher(href);
            if (matcher.find()) {
                aid = matcher.group(1);
            }

            matcher = oidPattern.matcher(href);
            if (matcher.find()) {
                oid = matcher.group(1);
            }

            if (aid != "" && oid != "") {
                Article article = new Article(connection, oid, aid);
                article.setTitle(title);
                article.setHasImages(hasImages);
                article.setPress(press);
                article.setTimestamp(timestamp);

                rankedNewsList.add(article);
            } else {
                logger.log(Level.SEVERE, "Unable to parse aid, oid from news article href.");
            }
        }

        return rankedNewsList;
    }

    public void retrieve() throws ServerException, InternalException
    {
        HttpResult result = mConnection.requestGet(getURL(), null, null);

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

        Element articleBodyContent = divs.get(0);
        mContent = ContentElements.parseElement(articleBodyContent);

        for (ContentElement e : mContent)
            if (e instanceof Image) {
                mHasImages = true;
                break;
            }

        divs = doc.select("h3[id=articleTitle]");

        if (divs.size() < 1) {
            logger.log(Level.SEVERE, "Unable to parse news title.");
            throw new ServerException();
        }

        mTitle = divs.get(0).text().trim();

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

        mCommentSize = Integer.parseInt(replyCounts.get(0).text());
    }

    public boolean isLiked()
    {
        return mLiked;
    }

    public void retrieveLikeStatus() throws ServerException, InternalException
    {
        String url = String.format("http://news.like.naver.com/likeIt/likeItContent.jsonp?serviceId=NEWS&displayId=NEWS&contentsId=ne_%s_%s&viewType=recommend", mOid, mAid);
        JSONObject result = requestLikeServer(url);

        if (result.has("likeItContentsYn")) {
            mLiked = result.getString("likeItContentsYn").equals("Y");
        } else {
            mLiked = false;
            logger.log(Level.WARNING, "Json object doesn't indicate whether user liked the content or not.");
        }

        if (!result.has("likeItContent") || result.get("likeItContent") == null) {
            logger.log(Level.SEVERE, "Json object doesn't contain 'likeItContent'.");
            throw new ServerException();
        }

        JSONObject likeItContent = result.getJSONObject("likeItContent");

        if (likeItContent == null) {
            logger.log(Level.SEVERE, "Unexpected absence of likeitcontent.");
            return;
        }

        int count = likeItContent.getInt("likeItCount");
        mLikes = count;
    }

    public Comment writeComment(String content) throws ServerException, InternalException
    {
        String gno = "news" + mOid + "," + mAid;

        HttpHeaderCollection header = new HttpHeaderCollection();
        header.put("charset", "utf-8");
        header.put("Host", "comment.news.naver.com");
        header.put("Origin", "http://comment.news.naver.com");
        header.put("Referer", "http://comment.news.naver.com");

        HttpForm formContent = new HttpForm();
        formContent.put("content", content);
        formContent.put("gno", gno);
        formContent.put("serviceId", "news");
        formContent.put("incomingType", "pc");
        formContent.put("validateBanWords", "true");

        JSONObject object = null;

        try {
            object = mConnection.requestJsonPost("http://comment.news.naver.com/api/comment/write.json", header, formContent);
        } catch (JSONErrorException e) {
            logger.log(Level.SEVERE, "Unexpected json error.", e);
            throw new ServerException(e.getMessage());
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

        Comment comment = new Comment(mConnection, this, array.getJSONObject(0));

        return comment;
    }

    protected JSONObject requestLikeServer(String url) throws InternalException, ServerException
    {
        HttpResult result = mConnection.requestGet(url, null, null);
        JSONObject object = null;

        try {
            String content = result.getContentAsString();
            content = content.replaceAll("[^(]+\\((.*)\\);$", "$1");
            object = new JSONObject(content);

            if (!object.has("code") || object.getInt("code") != 0 || !object.has("result")) {
                logger.log(Level.SEVERE, "Unexpected json format.");
                throw new ServerException();
            }

            object = object.getJSONObject("result");
        } catch (JSONException e) {
            logger.log(Level.SEVERE, "unexpected json error", e);
            throw new ServerException();
        }

        return object;
    }

    protected void likeOrUnlike(boolean shareTimeline, boolean like) throws InternalException, ServerException
    {
        String url = String.format("http://news.like.naver.com/likeIt/v1/%s.jsonp?serviceId=NEWS&contentsId=ne_%s_%s&lang=ko&timeLineShare=%s", like ? "likeItContentAdd" : "unLikeItContent", mOid, mAid, shareTimeline ? "Y" : "N");
        JSONObject result = requestLikeServer(url);

        if (result.has("resultStatusCode") && result.getInt("resultStatusCode") == (like ? 0 : 2003)) {
            if (result.has("likeItCount"))
                mLikes = result.getInt("likeItCount");
            else if (result.has("contents")) {
                JSONObject contentsObject = result.getJSONObject("contents");
                if (contentsObject != null && contentsObject.has("likeItCount"))
                    mLikes = contentsObject.getInt("likeItCount");
            }

            logger.log(Level.INFO, "like connection success.");

            mLiked = like;
        } else {
            logger.log(Level.WARNING, "like connection failed.");
            throw new ServerException();
        }
    }

    public void like(boolean shareTimeline) throws InternalException, ServerException
    {
        likeOrUnlike(shareTimeline, true);
    }

    public void cancelLike(boolean shareTimeline) throws InternalException, ServerException
    {
        likeOrUnlike(shareTimeline, false);
    }

    public List<Comment> getComments(int page, int pageSize, SortType sortType) throws InternalException, ServerException
    {
        String gno = "news" + mOid + "," + mAid;

        HttpForm formContent = new HttpForm();
        formContent.put("gno", gno);
        formContent.put("serviceId", "news");
        formContent.put("pageSize", String.valueOf(pageSize));
        formContent.put("page", String.valueOf(page));

        String sortBy = sortType.toString();

        formContent.put("sort", sortBy);

        JSONObject object = null;

        try {
            object = mConnection.requestJsonPost("http://comment.news.naver.com/api/comment/list.json", null, formContent);
        } catch (JSONErrorException e) {
            logger.log(Level.SEVERE, "Unexpected json error.", e);
            throw new InternalException();
        }

        List<Comment> commentList = new ArrayList<Comment>();

        if (!object.has("commentReplies")) {
            logger.log(Level.SEVERE, "Unexpected absence of 'message.result.commentReplies'");
            throw new ServerException("The server returned malformed json.");
        }

        JSONArray commentsJson = object.getJSONArray("commentReplies");
        for (int i = 0; i < commentsJson.length(); ++i) {
            JSONObject commentObject = commentsJson.getJSONObject(i);
            Comment comment = new Comment(mConnection, this, commentObject);
            commentList.add(comment);
        }

        return commentList;
    }

    public String getGno()
    {
        return "news" + mOid + "," + mAid;
    }

    public ContentElements getContentElements()
    {
        return mContent;
    }

    //Returns the representative Image object for the article.
    //Current implementation returns the index-0 Image if there is any.
    //However, it might be ideal if the method could discrimnate images
    //using more complicated algorithms.
    public Image getTopImage()
    {
        for (ContentElement ce : mContent)
            if (ce instanceof Image)
                return (Image) ce;

        return null;
    }

    public String getContent()
    {
        return mContent.toString();
    }

    public String getURL()
    {
        return String.format("http://news.naver.com/main/read.nhn?oid=%s&aid=%s", mOid, mAid);
    }
}
