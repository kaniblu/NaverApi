package com.kaniblu.naver.api.news.comment;

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

    public static SortType parse(String value)
    {
        String v = value.toLowerCase().trim();

        for (SortType s : values())
            if (s.mString.equals(v))
                return s;

        return null;
    }

    @Override
    public String toString()
    {
        return mString;
    }
}
