package com.kaniblu.naver.api.news;

import com.kaniblu.naver.api.InternalException;

import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.jsoup.nodes.TextNode;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

public abstract class ContentElement
{
    private static final Logger logger = Logger.getLogger(ContentElement.class.getCanonicalName());
    public abstract void loadFromElement(Element element) throws InternalException;
}
