package com.kaniblu.naver.test;

import java.io.IOException;

public class Console
{
    private final byte[] BUFFER = new byte[1024];

    public String read()
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

    public String readPw()
    {
        if (System.console() == null)
            return read();
        else
            return new String(System.console().readPassword());
    }

    public void writeln(String msg)
    {
        System.out.println(msg);
    }

    public void write(String msg)
    {
        System.out.print(msg);
    }

    protected static Integer tryParseInt(String str)
    {
        try {
            return Integer.parseInt(str);
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
