package com.kaniblu.naver.api.news;

import com.kaniblu.naver.api.InternalException;

import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.jsoup.nodes.TextNode;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ContentElements extends ArrayList<ContentElement>
{
    private static final Logger logger = Logger.getLogger(ContentElements.class.getCanonicalName());
    private static final Map<String, Class<?>> TAG_ELEMENT_MAP = new HashMap<String, Class<?>>()
    {
        {
            put("span.end_photo_org", Image.class);
            put("table", Image.class);
            put("img", Image.class);
            put("strong", Heading.class);
            put("h1", Heading.class);
            put("h2", Heading.class);
            put("h3", Heading.class);
            put("h4", Heading.class);
            put("p", Paragraph.class);
        }
    };
    private static final Set<String> IGNORED_TAGS = new TreeSet<String>()
    {
        {
            add("span");
            add("font");
            add("b");
            add("i");
        }
    };
    private static final Map<String, Pattern> PATTERN_CACHE = new HashMap<String, Pattern>();

    @Override
    public String toString()
    {
        String str = "";

        for (ContentElement e : this)
            str += e.toString() + "\n";

        return str;
    }

    private static Pattern getIgnorePattern(String tag)
    {
        if (!PATTERN_CACHE.containsKey(tag)) {
            Pattern pattern = Pattern.compile(String.format("<%s>([^<]*)</%s>", tag, tag));
            PATTERN_CACHE.put(tag, pattern);
        }

        return PATTERN_CACHE.get(tag);
    }

    public static ContentElements parseElement(Element element)
    {
        /*String html = element.html();

        /*for (String tag : IGNORED_TAGS) {
            Pattern pattern = getIgnorePattern(tag);
            while (true) {
                Matcher matcher = pattern.matcher(html);
                if (matcher.matches())
                    html = matcher.replaceAll("$1");
                else
                    break;
            }
        }

        element.html(html);*/
        ContentElements elements = new ContentElements();
        for(Node node : element.childNodes()) {
            if(node instanceof Element) {
                Element e = (Element) node;

                String tag = e.tagName();
                String css = ElementUtils.cssSelector(e);
                if (IGNORED_TAGS.contains(css)) {
                    ContentElements subElements = parseElement(e);
                    String str = subElements.toString();
                    if (elements.size() > 0 && elements.get(elements.size() - 1) instanceof Paragraph) {
                        Paragraph paragraph = (Paragraph)elements.get(elements.size() - 1);
                        paragraph.setText(paragraph.getText() + str);
                    } else {
                        Paragraph paragraph = new Paragraph(str);
                        elements.add(paragraph);
                    }
                } else if (TAG_ELEMENT_MAP.containsKey(css)) {
                    Class<?> c = TAG_ELEMENT_MAP.get(css);
                    ContentElement contentElement = null;
                    try {
                        contentElement = (ContentElement) c.newInstance();
                    } catch (InstantiationException ex) {
                        logger.log(Level.INFO, "instatiation error while instantiating a new BodyElement Object.", ex);
                        continue;
                    } catch (IllegalAccessException ex) {
                        logger.log(Level.INFO, "error occurred while instantiating a new BodyElement Object.", ex);
                        continue;
                    }
                    try {
                        contentElement.loadFromElement(e);
                    } catch (InternalException ex) {
                        logger.log(Level.INFO, "An error occurred while loading from element.", ex);
                        continue;
                    }
                    elements.add(contentElement);
                } else {
                    List<ContentElement> innerElements = parseElement(e);
                    elements.addAll(innerElements);
                }
            } else if(node instanceof TextNode) {
                TextNode t = (TextNode) node;
                String text = t.getWholeText();
                if (text != null && text.trim().length() > 0)
                    elements.add(new Paragraph(text.trim()));
            }
        }

        return elements;
    }
}
