package com.kaniblu.naver.api;

import org.joda.time.DateTime;

import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Time
{
    private static Logger logger = Logger.getLogger(Time.class.getCanonicalName());

    protected static Pattern NAVER_TIMESTAMP_PATTERN = Pattern.compile("^(\\d{4})\\.(\\d{2})\\.(\\d{2}) ([^\\s]*) (\\d{1,2}):(\\d{2})$");

    protected DateTime mDateTime;

    public static Time parse(String str)
    {
        Matcher m = NAVER_TIMESTAMP_PATTERN.matcher(str);

        if (!m.matches())
            return null;

        int year = Integer.valueOf(m.group(1));
        int month = Integer.valueOf(m.group(2));
        int day = Integer.valueOf(m.group(3));
        int hour = Integer.valueOf(m.group(5)) % 12;
        int minute = Integer.valueOf(m.group(6));

        String amPm = m.group(4);
        if (!amPm.equals("오전") && !amPm.equals("오후")) {
            logger.log(Level.SEVERE, "AM/PM indicator unrecognized.");
            return null;
        }

        if (amPm.equals("오후"))
            hour += 12;

        Time time = new Time();
        time.mDateTime = new DateTime(year, month, day, hour, minute);
        return time;
    }

    public DateTime toJodaTime()
    {
        return mDateTime;
    }
}
