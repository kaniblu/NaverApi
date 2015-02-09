package com.kaniblu.naver.api;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Scanner;

public class Utils
{
    public static String toString(InputStream stream, String encoding)
    {
        Scanner s = new Scanner(stream, encoding);
        s.useDelimiter("\\A");
        return s.hasNext() ? s.next() : "";
    }

    public static byte[] toByteArray(InputStream stream) throws IOException
    {
        ByteArrayOutputStream byteStream = new ByteArrayOutputStream();

        int read;
        byte[] buffer = new byte[4096];

        while ((read = stream.read(buffer, 0, buffer.length)) != -1)
            byteStream.write(buffer, 0, read);

        return byteStream.toByteArray();
    }
}