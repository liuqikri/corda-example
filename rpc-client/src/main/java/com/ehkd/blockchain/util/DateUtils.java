package com.ehkd.blockchain.util;

import java.time.Instant;
import java.util.Date;

public class DateUtils {

    public static Date instantToDate(Instant instant) {
        if(instant != null) {
            return Date.from(instant);
        }
        return null;
    }

}
