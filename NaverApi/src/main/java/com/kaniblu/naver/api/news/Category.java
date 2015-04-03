package com.kaniblu.naver.api.news;

public enum Category
{
    ALL(0),
    POLITICS(100),
    ECONS(101),
    SOCIAL(102),
    CULT(103),
    WORLD(104),
    TECH(105),
    CELEB(106),
    SPORTS(107);

    private int mId;

    private Category(int id)
    {
        mId = id;
    }

    public static Category parse(String value)
    {
        try {
            int id = Integer.valueOf(value);
            for (Category c : values())
                if (c.mId == id)
                    return c;

            return null;
        } catch (NumberFormatException e) {
            return null;
        }
    }

    @Override
    public String toString()
    {
        return String.format("%03d", mId);
    }
}
