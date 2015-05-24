package com.kaniblu.naver.api.news;

import org.jsoup.nodes.Element;

public class ElementUtils
{
    public static String cssSelector(Element element)
    {
        String css = element.cssSelector();

        if (css == null)
            return null;

        String[] tokens = css.split(">");

        if (tokens.length < 1)
            return null;

        String token = tokens[tokens.length - 1];
        if (token == null || token.trim().length() <= 0)
            return null;

        return token.trim();
    }
}
