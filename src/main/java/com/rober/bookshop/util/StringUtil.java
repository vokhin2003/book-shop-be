package com.rober.bookshop.util;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

public class StringUtil {
    public static String formatPrice(BigDecimal price) {
        DecimalFormat formatter = new DecimalFormat("#,###");

        return formatter.format(price);
    }


    public static String formatTime(Instant instant) {
        //format time
        ZoneId zoneId = ZoneId.of("Asia/Ho_Chi_Minh");

        DateTimeFormatter formatter = DateTimeFormatter
                .ofPattern("hh:mm, dd/MM/yyyy")
                .withZone(zoneId);
        return formatter.format(instant);
    }
}
