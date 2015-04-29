package com.kaniblu.naver.api.news;

import com.kaniblu.naver.api.InternalException;

import org.jsoup.nodes.Element;

public class Paragraph extends ContentElement
{
    protected String mText;

    public Paragraph()
    {

    }

    public Paragraph(String text)
    {
        mText = text;
    }

    @Override
    public void loadFromElement(Element element) throws InternalException
    {
        mText = element.text().trim();
    }

    public String getText()
    {
        return mText;
    }

    public void setText(String text)
    {
        mText = text;
    }

    @Override
    public String toString()
    {
        return mText;
    }
}
