package com.kaniblu.naver.api.news;

import com.kaniblu.naver.api.InternalException;

import org.jsoup.nodes.Element;

public class Heading extends ContentElement
{
    protected String mTitle;

    public Heading()
    {

    }

    public Heading(String title)
    {
        mTitle = title;
    }

    @Override
    public void loadFromElement(Element element) throws InternalException
    {
        mTitle = element.text().trim();
    }

    public String getTitle()
    {
        return mTitle;
    }

    public void setTitle(String title)
    {
        mTitle = title;
    }

    @Override
    public String toString()
    {
        return mTitle;
    }
}
