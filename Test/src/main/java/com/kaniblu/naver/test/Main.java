package com.kaniblu.naver.test;

import com.kaniblu.naver.api.*;

import com.kaniblu.naver.api.news.Article;
import com.kaniblu.naver.api.news.Category;
import com.kaniblu.naver.api.news.comment.Comment;
import com.kaniblu.naver.api.news.comment.SortType;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import java.util.List;
import java.util.logging.LogManager;

public class Main
{
    public static void main(String[] args) throws Exception
    {
        LogManager.getLogManager().reset();

        final Console c = new Console();
        final Connection connection = new Connection();
        connection.requestCookies();

        InteractiveConsole.OnMenuSelectedListener topLevelListener = new InteractiveConsole.OnMenuSelectedListener()
        {
            @Override
            public boolean onMenuSelected(Console console, int id)
            {
                try {
                    switch (id) {
                        case 1:
                            console.writeln("Enter the following parameters. (input empty string to open a default one)");
                            console.write("oid: ");
                            String oidStr = console.read();
                            String aidStr = null;

                            if (oidStr.length() == 0) {
                                oidStr = "008";
                                aidStr = "0003426173";
                            } else {
                                console.write("aid: ");
                                aidStr = console.read();
                            }

                            final Article article = new Article(connection, oidStr, aidStr);
                            article.retrieve();

                            console.writeln(article.getContent());

                            InteractiveConsole.OnMenuSelectedListener articleMenuListener = new InteractiveConsole.OnMenuSelectedListener()
                            {
                                @Override
                                public boolean onMenuSelected(Console console, int index)
                                {
                                    try {
                                        switch (index) {
                                            case 1:
                                                console.writeln("Enter the following parameters.");
                                                console.write("content: ");

                                                String content = console.read();

                                                Comment writtenComment = article.writeComment(content);

                                                console.writeln("Comment was successfully written.");

                                                return true;
                                            case 2:
                                                List<Comment> comments = null;

                                                comments = article.getComments(0, 9999, SortType.SCORE);

                                                for (Comment comment : comments)
                                                    console.writeln(String.format("[%s] (%d/%d) %s", comment.getCommentor().getUsername(), comment.getNUp(), comment.getNDown(), comment.getContent()));

                                                return true;
                                            case 3:
                                                article.retrieveLikeStatus();
                                                console.writeln("There are " + article.getLikes() + " likes.");
                                                console.writeln(String.format("User has %sliked the article.", article.isLiked() ? "" : "not "));
                                                return true;
                                            case 4:
                                                article.like(false);
                                                console.writeln("Like success: " + article.getLikes() + " likes.");
                                                return true;
                                            case 5:
                                                article.cancelLike(false);

                                                console.writeln("Unlike success: " + article.getLikes() + " likes.");
                                                return true;
                                            default:
                                                return true;
                                        }
                                    } catch (Exception e) {
                                        console.writeln("An exception occurred.");
                                        return true;
                                    }
                                }
                            };
                            InteractiveConsole articleConsole = new InteractiveConsole();
                            articleConsole.setHeaderTitle("What to do with the article?");
                            articleConsole.addMenu(1, "Write a comment.", articleMenuListener);
                            articleConsole.addMenu(2, "Get all comments.", articleMenuListener);
                            articleConsole.addMenu(3, "Get like status.", articleMenuListener);
                            articleConsole.addMenu(4, "Like the article.", articleMenuListener);
                            articleConsole.addMenu(5, "Unlike the article.", articleMenuListener);
                            articleConsole.run();

                            return true;
                        case 2:
                            String oid = null;
                            String aid = null;
                            Integer commentId = null;

                            console.writeln("Enter following information (input empty string to use default)");
                            console.write("Oid: ");

                            oid = console.read();

                            if (oid.length() == 0) {
                                oid = "008";
                                aid = "0003426173";
                                commentId = 1199273;
                            } else {

                                console.write("Aid: ");
                                aid = console.read();

                                console.write("Comment Id: ");
                                commentId = Integer.valueOf(console.read());
                            }

                            Article art = new Article(connection, oid, aid);
                            Comment comment = new Comment(connection, art, commentId);
                            List<Comment> commentReplies = null;
                            try {
                                comment.retrieve();
                                commentReplies = comment.getComments(1, 20, SortType.SCORE);
                            } catch (Exception e) {
                                console.writeln("An exception occurred.");
                                return true;
                            }

                            console.writeln(comment.getContent());
                            for (Comment c : commentReplies)
                                console.writeln(c.getContent());

                            return true;
                        case 3:
                        case 8:
                        case 9:
                            for (Category c : Category.values())
                                console.writeln(String.format("%s(%s)", c.name(), c.toString()));

                            console.write("Choose a category (e.g. 000): ");
                            Category category = Category.parse(console.read());
                            console.write("Choose a date (yyyyMMdd): ");
                            String date = console.read();

                            if (!date.matches("\\d{4}\\d{2}\\d{2}")) {
                                console.writeln("Wrong date format");
                                return true;
                            }

                            DateTimeFormatter formatter = DateTimeFormat.forPattern("yyyyMMdd");
                            DateTime dateTime = formatter.parseDateTime(date);

                            List<Article> newsList = null;

                            switch (id) {
                                case 3:
                                    newsList = Article.retrieveDailyTopNews(connection, category, dateTime);
                                    break;
                                case 8:
                                    newsList = Article.retrieveWeeklyTopNews(connection, category, dateTime);
                                    break;
                                case 9:
                                    newsList = Article.retrieveWeeklyMostCommentedNews(connection, category, dateTime);
                                    break;
                            }

                            console.writeln(newsList.size() + " ranked news retrieved");

                            for (Article a : newsList) {
                                console.writeln(String.format("%s / %s / %s", a.getGno(), a.hasImages() ? "[P]" : "[ ]", a.getTitle()));
                            }

                            return true;
                        case 4:
                            console.writeln("Retrieving current user info.");

                            User user = new User(connection);
                            user.retrieve();

                            console.writeln("Current user is " + (user.isLoggedIn() ? "" : "not ") + "logged in.");

                            if (user.isLoggedIn()) {
                                console.writeln("Username: " + user.getUsername());
                                console.writeln("Encoded Username: " + user.getEncodedUsername());
                                console.writeln("Nickname: " + user.getNickname());
                                console.writeln("User type: " + user.getType());
                                console.writeln("Is comment writable?: " + user.isCommentWritable());
                                console.writeln("Is using real name?: " + user.isNameReal());
                            }

                            return true;
                        case 5:
                            c.write("Username: ");
                            String username = c.read();

                            c.write("Password: ");
                            String password = c.readPw();

                            connection.setPassword(password);
                            connection.setUsername(username);
                            connection.login();

                            c.writeln("Login success!");

                            return true;
                        case 6:
                            c.writeln("Logging out...");
                            connection.logout();

                            return true;
                        case 7:
                            c.writeln("Retrieving all comments posted by the user...");
                            user = new User(connection);
                            List<Comment> comments = user.getComments(1, 20, SortType.SCORE);

                            for (Comment comm : comments)
                                c.writeln(String.format("[%s] (%d/%d) %s", comm.getArticle().getGno(), comm.getNUp(), comm.getNDown(), comm.getContent()));

                            return true;
                        default:
                            return true;
                    }
                } catch (Exception e) {
                    console.writeln("An exception occurred.");
                    return true;
                }
            }
        };

        InteractiveConsole topLevel = new InteractiveConsole();
        topLevel.addMenu(5, "Log in.", topLevelListener);
        topLevel.addMenu(6, "Log out.", topLevelListener);
        topLevel.addMenu(1, "Read an article.", topLevelListener);
        topLevel.addMenu(2, "Read a comment.", topLevelListener);
        topLevel.addMenu(3, "Retrieve daily popular news.", topLevelListener);
        topLevel.addMenu(8, "Retrieve weekly popular news.", topLevelListener);
        topLevel.addMenu(9, "Retrieve weekly most commented news.", topLevelListener);
        topLevel.addMenu(4, "Check user status.", topLevelListener);
        topLevel.addMenu(7, "Get all comments posted by the user.", topLevelListener);
        topLevel.run();
    }
}
