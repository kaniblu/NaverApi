package com.kaniblu.naver.test;

import com.kaniblu.naver.api.Connection;
import com.kaniblu.naver.api.NewsArticle;
import com.kaniblu.naver.api.NewsComment;
import com.kaniblu.naver.api.ServerException;

import java.io.IOException;
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

    public static void main(String[] args)
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
            write("Username: ");
            String username = read();

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
            write("Your choice? [1-5]: ");

            Integer choice = tryParseInt(read());

            if (choice == null || choice < 1 || choice > 5) {
                writeln("Choose a number between 1 to 5.");
                continue;
            }

            switch (choice) {
                case 1:
                    writeln("Enter the following parameters.");
                    write("oid: ");
                    String oidStr = read();
                    write("aid: ");
                    String aidStr = read();

                    NewsArticle article = null;
                    try {
                        article = new NewsArticle(connection, oidStr, aidStr);
                        article.retrieveContent();
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
                        writeln("3. go back.");
                        write("Your choice [1-3]: ");

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
                                    e.printStackTrace();
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
                                    e.printStackTrace();
                                    continue;
                                }

                                for (NewsComment comment : comments)
                                    writeln("(" + comment.getNUp() + "/" + comment.getNDown() + ")" + comment.getContent());

                                break;
                            case 3:
                                innerloop = false;
                                break;
                            default:
                                writeln("Unknown option.");
                                break;
                        }
                    }
                    break;
                case 5:
                    writeln("What date(YYYYMMDD) do you want to get?");

                    String date = read();
                    if (!date.matches("\\d{4}\\d{2}\\d{2}")) {
                        writeln("Wrong date format");
                        break;
                    }

                    try {
                        List<NewsArticle> newsList = NewsArticle.getDailyRankedNewsList(new Connection(), date);
                        writeln(newsList.size() + " ranked news retrieved");

                        for (NewsArticle a : newsList) {
                            writeln(a.getGno());
                        }
                    } catch (ServerException e) {
                        // TODO Auto-generated catch block
                        writeln("An exception occurred.");
                        e.printStackTrace();
                        break;
                    }

                    break;
            }
        }
    }
}
