package com.xlw.levyx.dblog;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by manbu on 3/21/16.
 */
public class Lite {

    private String name;
    private long start;
    private int duration;

    private static final Pattern pattern = Pattern.compile("^start\\["+ "(\\d+)" +"\\]\\s+time\\["+ "(\\d+)" +"\\]\\s+tag\\["+ "(\\w+)" +"\\]$");

    public Lite(String name, long start , int duration) {
        this.name = name;
        this.start = start;
        this.duration = duration;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public long getStart() {
        return start;
    }

    public void setStart(long start) {
        this.start = start;
    }

    public int getDuration() {
        return duration;
    }

    public void setDuration(int duration) {
        this.duration = duration;
    }

    public static Lite from(String message) {

        Matcher matcher = pattern.matcher(message);

        if(matcher.find()) {

            Long start = Long.parseLong(matcher.group(1));
            int duration = Integer.parseInt(matcher.group(2));
            String name = matcher.group(3);

            return new Lite(name, start, duration);

        } else {

            return null;
        }
    }

    @Override
    public String toString() {
        return "Lite{" +
                "name='" + name + '\'' +
                ", start=" + start +
                ", duration=" + duration +
                '}';
    }
}
