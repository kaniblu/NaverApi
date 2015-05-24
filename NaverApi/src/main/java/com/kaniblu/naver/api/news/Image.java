package com.kaniblu.naver.api.news;

import com.kaniblu.naver.api.InternalException;

import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.jsoup.nodes.TextNode;
import org.jsoup.select.Elements;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Image extends ContentElement
{
    private static final Logger logger = Logger.getLogger(Image.class.getCanonicalName());

    protected URL mUrl;
    protected String mTitle;
    protected String mCaption;

    public Image()
    {

    }

    public Image(String url, String title, String caption)
    {
        try {
            mUrl = new URL(url);
        } catch (MalformedURLException e) {
            mUrl = null;
        }

        mTitle = title;
        mCaption = caption;
    }

    @Override
    public void loadFromElement(Element root) throws InternalException
    {
        Element imageElement = null;
        String rootTag = root.tagName();

        if (rootTag.equals("table")) {
            Elements imgElements = root.select("img");

            if (imgElements.size() != 1) {
                logger.log(Level.WARNING, "table element does not contain exactly one img tag.");
                throw new InternalException();
            }

            imageElement = imgElements.get(0);

            Element table = root;
            Elements rows = table.select("tr");
            boolean hasTitle = false;
            if (rows.size() > 1) {
                Element titleRow = rows.get(1);
                if (titleRow.childNodes().size() > 0) {
                    Node n = titleRow.childNodes().get(0);
                    if (n instanceof Element) {
                        Element potentialTitle = (Element)n;
                        if (potentialTitle.tagName().equals("strong") || potentialTitle.tagName().equals("b")) {
                            mTitle = potentialTitle.text();
                            hasTitle = true;
                        }
                    }
                }
            }

            ContentElements captionElements = new ContentElements();
            for (Element row : rows)
                for (Node n : row.childNodes()) {
                    if (hasTitle) {
                        hasTitle = false;
                        continue;
                    }

                    if (n instanceof TextNode) {
                        TextNode tn = (TextNode) n;
                        captionElements.add(new Paragraph(tn.getWholeText()));
                    } else {
                        ContentElements subCaptionElements = ContentElements.parseElement((Element)n);
                        captionElements.addAll(subCaptionElements);
                    }
                }

            mCaption = captionElements.toString();
        } else if (rootTag.equals("span")) {
            Elements imgElements = root.select("img");

            if (imgElements.size() != 1) {
                logger.log(Level.WARNING, "span element does not contain exactly one img tag.");
                throw new InternalException();
            }

            imageElement = imgElements.get(0);
            Elements emElements = root.select("em.img_desc");

            if (emElements.size() > 0) {
                Element emElement = emElements.get(0);
                ContentElements parsed = ContentElements.parseElement(emElement);
                mCaption = parsed.toString();
            }
        } else if (rootTag.equals("img"))
            imageElement = root;

        if (!imageElement.hasAttr("src"))
            logger.log(Level.WARNING, "img tag doesn't contain src.");

        String urlString = imageElement.attr("src");
        try {
            mUrl = new URL(urlString);
        } catch (MalformedURLException e) {
            mUrl = null;
        }
    }

    public URL getUrl()
    {
        return mUrl;
    }

    public void setUrl(String url)
    {
        try {
            mUrl = new URL(url);
        } catch (MalformedURLException e) {
            mUrl = null;
        }
    }

    public String getCaption()
    {
        return mCaption;
    }

    public void setCaption(String caption)
    {
        mCaption = caption;
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
        return String.format("[Image] %s - %s", mTitle, mCaption);
    }
}
