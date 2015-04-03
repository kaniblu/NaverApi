package com.kaniblu.naver.api;

import com.kaniblu.naver.http.HttpForm;
import com.kaniblu.naver.http.HttpHeaderCollection;
import com.kaniblu.naver.http.HttpResult;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.jsoup.Jsoup;
import org.jsoup.helper.StringUtil;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.jsoup.nodes.TextNode;
import org.jsoup.select.Elements;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class NewsArticle
{
    private static final DateTimeFormatter DAILYRANK_TIMESTAMP_FORMAT = DateTimeFormat.forPattern("yyyy-MM-dd");
    private static final Logger logger = Logger.getLogger(NewsArticle.class.getCanonicalName());

    private static final String EXCLUDE_TAG = "table";
    private static final String INCLUDE_TAG = "b,i,a";
    private static final String LINE_SPLIT_TAG = "p,hr,br";
    private Set<String> excludeTag;
    private Set<String> includeTag;
    private Set<String> lineSplitTag;

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

    public List<URL> getImageURLs()
    {
        return mImageURLs;
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
    protected List<URL> mImageURLs = new ArrayList<URL>();
    protected List<String> mHighlights = new ArrayList<String>();

    public List<String> getHighlights()
    {
        return mHighlights;
    }

    protected Map<URL, String> mImageCaptions = new HashMap<URL, String>();
    protected Connection mConnection;
    protected String mOid;
    protected String mAid;
    protected String mContent;
    protected DateTime mTimestamp;
    protected int mCommentSize;
    protected String mTitle;
    protected String mPress;
    protected String mCategory;
    protected int mLikes;

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

    public void setContent(String content)
    {
        mContent = content;
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

    public String getImageCaption(URL url)
    {
        if (mImageCaptions.containsKey(url))
            return mImageCaptions.get(url);
        else
            return null;
    }

    public NewsArticle(Connection connection, String oid, String aid)
    {
        mConnection = connection;
        mOid = oid;
        mAid = aid;
    }

    public NewsArticle()
    {

    }

    public static List<NewsArticle> getDailyRankedNewsList(Connection connection, DateTime date) throws ServerException
    {
        if (date == null)
            date = DateTime.now();

        String dateString = String.format("%04d%02d%02d", date.getYear(), date.getMonthOfYear(), date.getDayOfMonth());
        HttpResult result = connection.requestGet("http://news.naver.com/main/ranking/popularDay.nhn?rankingType=popular_day&sectionId=000&date=" + dateString, null, null);

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

        List<NewsArticle> rankedNewsList = new LinkedList<NewsArticle>();

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

            Elements imgIcons = e.select("img[alt=포토]");

            if (imgIcons.size() > 0)
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
                NewsArticle article = new NewsArticle(connection, oid, aid);
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

    public void makeHtmlTree(List<String> tree, List<Node> nodes) {
        if(excludeTag == null) {
            excludeTag = new HashSet<String>();
            for(String s : EXCLUDE_TAG.split(",")) {
                excludeTag.add(s);
            }
        }

        if(includeTag == null) {
            includeTag = new HashSet<String>();
            for(String s : INCLUDE_TAG.split(",")) {
                includeTag.add(s);
            }
        }

        if(lineSplitTag == null) {
            lineSplitTag = new HashSet<String>();
            for (String s : LINE_SPLIT_TAG.split(",")) {
                lineSplitTag.add(s);
            }
        }

        for(Node node : nodes) {
            if(node instanceof Element) {
                Element e = (Element) node;

                if(!excludeTag.contains(e.tagName())) {
                    if(lineSplitTag.contains(e.tagName())) {
                        tree.add("\n");
                        makeHtmlTree(tree, e.childNodes());
                    } else if(includeTag.contains(e.tagName())) {
                        tree.add("<" + e.tagName() + ">");
                        makeHtmlTree(tree, e.childNodes());
                        tree.add("</" + e.tagName() + ">");
                    } else {
                        makeHtmlTree(tree, e.childNodes());
                    }
                }
            } else if(node instanceof TextNode) {
                TextNode t = (TextNode) node;
                tree.add(t.getWholeText().replaceAll("[\t\r]", ""));
            }
        }
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

        List<String> tree = new ArrayList<String>();
        makeHtmlTree(tree, divs.get(0).childNodes());
        mContent = StringUtil.join(tree, "");
        mContent = mContent.trim();
        mContent = mContent.replace("\u00a0", "\n");
        mContent = mContent.replaceAll("\n{3,}", "\n\n");
        mContent = mContent.replaceAll("\n[ ]+", "\n");

        String[] strings = mContent.split("\n");
        for(int i = 0; i < strings.length; i++) {
            String s = strings[i].trim();

            if(s.equals("")) {
                continue;
            }

            if(s.charAt(s.length() - 1) != '.' && s.charAt(s.length() - 1) != ']') {
                mHighlights.add(s);
                strings[i] = "";
            } else {
                break;
            }
        }

        mContent = StringUtil.join(Arrays.asList(strings), "\n");
        mContent = mContent.trim();

        Elements imageTags = divs.select("img");

        for (Element element : imageTags) {
            if (!element.hasAttr("src"))
                continue;

            String caption = null;

            Element table = element;

            while (table != null && !table.tagName().equals("table"))
                table = table.parent();

            if (table == null)
                logger.info("No image caption exists.");
            else
                caption = table.text().trim();

            try {
                URL imageURL = new URL(element.attr("src"));

                if (caption != null)
                    mImageCaptions.put(imageURL, caption);

                mImageURLs.add(imageURL);
            } catch (MalformedURLException e) {
                logger.warning("Malformed url in img src.");
            }
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

    public void retrieveLikeCount() throws ServerException, InternalException
    {
        String url = String.format("http://news.like.naver.com/likeIt/likeItContent.jsonp?serviceId=NEWS&displayId=NEWS&contentsId=ne_%s_%s&viewType=recommend", mOid, mAid);
        JSONObject result = null;
        try {
            result = mConnection.requestJsonGet(url, null);
        } catch (JSONErrorException e) {
            logger.log(Level.SEVERE, "Json error detected during like count retrieval", e);
            return;
        }

        JSONObject likeItContent = result.getJSONObject("likeItContent");

        if (likeItContent == null) {
            logger.log(Level.SEVERE, "Unexpected absence of likeitcontent.");
            return;
        }

        int count = likeItContent.getInt("likeItCount");
        mLikes = count;
    }

    public NewsComment writeComment(String content) throws ServerException, InternalException
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

        NewsComment comment = new NewsComment(mConnection, this, array.getJSONObject(0));

        return comment;
    }

    protected void requestLikeServer(String url, int expectedStatusCode) throws InternalException, ServerException
    {
        JSONObject result = null;

        try {
            result = mConnection.requestJsonGet(url, null);
        } catch (JSONErrorException e) {
            logger.log(Level.SEVERE, "unexpected json error", e);
            return;
        }

        if (result.has("resultStatusCode") && result.getInt("resultStatusCode") == expectedStatusCode) {
            if (result.has("likeItCount"))
                mLikes = result.getInt("likeItCount");
            else if (result.has("contents")) {
                JSONObject contentsObject = result.getJSONObject("contents");
                if (contentsObject != null && contentsObject.has("likeItCount"))
                    mLikes = contentsObject.getInt("likeItCount");
            }

            logger.log(Level.INFO, "like connection success.");
        }
        else {
            logger.log(Level.WARNING, "like connection failed.");
            throw new ServerException();
        }
    }
    public void like(boolean shareTimeline) throws InternalException, ServerException
    {
        String url = String.format("http://news.like.naver.com/likeIt/v1/likeItContentAdd.jsonp?serviceId=NEWS&contentsId=ne_%s_%s&lang=ko&timeLineShare=%s", mOid, mAid, shareTimeline ? "Y" : "N");
        requestLikeServer(url, 0);
    }

    public void cancelLike(boolean shareTimeline) throws InternalException, ServerException
    {
        String url = String.format("http://news.like.naver.com/likeIt/v1/unLikeItContent.jsonp?serviceId=NEWS&contentsId=ne_%s_%s&lang=ko&timeLineShare=%s", mOid, mAid, shareTimeline ? "Y" : "N");
        requestLikeServer(url, 2003);
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
            logger.log(Level.SEVERE, "Unexpected json error.", e);
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

    public String getURL()
    {
        return String.format("http://news.naver.com/main/read.nhn?oid=%s&aid=%s", mOid, mAid);
    }
}
