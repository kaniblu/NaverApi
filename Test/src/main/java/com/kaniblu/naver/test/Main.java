package com.kaniblu.naver.test;

import com.kaniblu.naver.api.*;
import com.kaniblu.naver.http.HttpClient;
import com.kaniblu.naver.http.HttpForm;
import com.kaniblu.naver.http.HttpHeaderCollection;
import com.kaniblu.naver.http.HttpResult;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import java.io.IOException;
import java.net.URL;
import java.util.Arrays;
import java.util.List;
import java.util.logging.LogManager;

public class Main
{
    private static final byte[] BUFFER = new byte[1024];

    public static String read()
    {
        int read = 0;
        try {
            read = System.in.read(BUFFER);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }

        return new String(BUFFER, 0, read > 0 ? read - 1 : read).trim();
    }

    public static void writeln(String msg)
    {
        System.out.println(msg);
    }

    public static void write(String msg)
    {
        System.out.print(msg);
    }

    public static Integer tryParseInt(String str)
    {
        try {
            return Integer.parseInt(str);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    public static void main(String[] args) throws Exception
    {
        runShell();
    }

    public static void runShell()
    {
        LogManager.getLogManager().reset();

        Connection connection = null;

        try {
            connection = new Connection();
            connection.requestCookies();
        } catch (Exception e) {
            writeln("Failed to initialize the connection.");
            writeln("Terminating...");
            return;
        }

        while (true) {
            writeln("Please login! (input empty string to skip)");

            write("Username: ");
            String username = read();

            if (username.length() == 0)
                break;

            write("Password: ");
            String password = read();

            connection.setPassword(password);
            connection.setUsername(username);

            try {
                connection.login();
            } catch (Exception e) {
                e.printStackTrace();
                writeln("Try again.");
                continue;
            }
            writeln("Login success!");
            break;
        }

        while (true) {
            writeln("Choose a task: ");
            writeln("1. Read an article");
            writeln("2. Read a comment");
            writeln("3. Write a comment");
            writeln("4. Delete a comment");
            writeln("5. Get daily news list");
            writeln("6. AuthCheck");
            write("Your choice? [1-6]: ");

            Integer choice = tryParseInt(read());

            if (choice == null || choice < 1 || choice > 6) {
                writeln("Choose a number between 1 to 6.");
                continue;
            }

            switch (choice) {
                case 1:
                    writeln("Enter the following parameters. (input empty string to open a default one)");
                    write("oid: ");
                    String oidStr = read();
                    String aidStr = null;

                    if (oidStr.length() == 0) {
                        oidStr = "008";
                        aidStr = "0003426173";
                    } else {
                        write("aid: ");
                        aidStr = read();
                    }

                    NewsArticle article = null;
                    try {
                        article = new NewsArticle(connection, oidStr, aidStr);
                        article.retrieve();
                    } catch (Exception e) {
                        e.printStackTrace();
                        writeln("Encountered an error.");
                        continue;
                    }

                    writeln(article.getContent());

                    boolean innerloop = true;
                    while (innerloop) {
                        writeln("What to do with the article?");
                        writeln("1. write a comment.");
                        writeln("2. get all comments.");
                        writeln("3. get like count.");
                        writeln("4. like the article.");
                        writeln("5. unlike the article.");
                        writeln("6. go back.");
                        write("Your choice [1-5]: ");

                        Integer result = tryParseInt(read());

                        if (result == null) {
                            writeln("Try again.");
                            continue;
                        }

                        switch (result) {
                            case 1:
                                writeln("Enter the following parameters.");
                                write("content: ");

                                String content = read();

                                NewsComment writtenComment = null;

                                try {
                                    article.writeComment(content);
                                } catch (Exception e) {
                                    writeln("Encountered an error.");
                                    continue;
                                }

                                writeln("Comment was successfully written.");
                                break;
                            case 2:
                                List<NewsComment> comments = null;

                                try {
                                    comments = article.getComments(0, 9999, NewsComment.SortType.SCORE);
                                } catch (Exception e) {
                                    writeln("An exception occurred.");
                                    continue;
                                }

                                for (NewsComment comment : comments)
                                    writeln("(" + comment.getNUp() + "/" + comment.getNDown() + ")" + comment.getContent());

                                break;
                            case 3:
                                try {
                                    article.retrieveLikeCount();
                                } catch (Exception e) {
                                    writeln("An exception occurred.");
                                    continue;
                                }

                                writeln("There are " + article.getLikes() + " likes.");
                                break;
                            case 4:
                                try {
                                    article.like(false);
                                } catch (Exception e) {
                                    writeln("An exception occurred.");
                                    continue;
                                }

                                writeln("Like success: " + article.getLikes() + " likes.");
                                break;
                            case 5:
                                try {
                                    article.cancelLike(false);
                                } catch (Exception e) {
                                    writeln("An exception occurred.");
                                    continue;
                                }

                                writeln("Unlike success: " + article.getLikes() + " likes.");
                                break;
                            case 6:
                                innerloop = false;
                                break;
                            default:
                                writeln("Unknown option.");
                                break;
                        }
                    }
                    break;
                case 2:
                    String oid = null;
                    String aid = null;
                    Integer commentId = null;

                    writeln("Enter following information (input empty string to use default)");
                    write("Oid: ");

                    oid = read();

                    if (oid.length() == 0) {
                        oid = "008";
                        aid = "0003426173";
                        commentId = 1199273;
                    } else {

                        write("Aid: ");
                        aid = read();

                        write("Comment Id: ");
                        commentId = Integer.valueOf(read());
                    }

                    NewsArticle art = new NewsArticle(connection, oid, aid);
                    NewsComment comment = new NewsComment(connection, art, commentId);
                    List<NewsComment> commentReplies = null;
                    try {
                        comment.retrieve();
                        commentReplies = comment.getComments(1, 20, NewsComment.SortType.SCORE);
                    } catch (Exception e) {
                        writeln("An exception occurred.");
                        continue;
                    }

                    writeln(comment.getContent());
                    for (NewsComment c : commentReplies)
                        writeln(c.getContent());

                    break;
                case 5:
                    writeln("What date(YYYYMMDD) do you want to get?");

                    String date = read();

                    if (!date.matches("\\d{4}\\d{2}\\d{2}")) {
                        writeln("Wrong date format");
                        break;
                    }

                    DateTimeFormatter formatter = DateTimeFormat.forPattern("yyyyMMdd");
                    DateTime dateTime = formatter.parseDateTime(date);

                    try {
                        List<NewsArticle> newsList = NewsArticle.getDailyRankedNewsList(new Connection(), dateTime);
                        writeln(newsList.size() + " ranked news retrieved");

                        for (NewsArticle a : newsList) {
                            writeln(a.getGno() + " / " + a.getTitle());
                        }
                    } catch (ServerException e) {
                        // TODO Auto-generated catch block
                        writeln("An exception occurred.");
                        e.printStackTrace();
                        break;
                    }

                    break;
                case 6:
                    writeln("Trying to send an AuthCheck POST.");

                    try {
                        JSONObject object = connection.authCheck();
                        writeln(object.toString());
                    } catch (ServerException e) {
                        writeln("A server error occurred.");
                        e.printStackTrace();
                    } catch (InternalException e) {
                        writeln("An internal error occurred.");
                        e.printStackTrace();
                    }

                    break;
            }
        }
    }
}
