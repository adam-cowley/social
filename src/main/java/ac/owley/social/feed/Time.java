package ac.owley.social.feed;

import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.TimeZone;

public class Time
{

    public static final ZoneId utc = TimeZone.getTimeZone("UTC").toZoneId();

    public static final DateTimeFormatter formatter = DateTimeFormatter
            .ofPattern("yyyy_MM_dd")
            .withZone(utc);

    public final static String POSTED_ON = "POSTED_ON_%s";
}
