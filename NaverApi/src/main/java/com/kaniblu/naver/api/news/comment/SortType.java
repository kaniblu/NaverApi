package com.kaniblu.naver.api.news.comment;

/**
* Created by Kani on 4/3/2015.
*/
public enum SortType
{
    SCORE("likability"),
    DATE_ASC("oldest"),
    DATE_DESC("newest"),
    NREPLY_DESC("replycount");

    private String mString;

    private SortType(String str)
    {
        mString = str;
    }

    @Override
    public String toString()
    {
        return mString;
    }
}
