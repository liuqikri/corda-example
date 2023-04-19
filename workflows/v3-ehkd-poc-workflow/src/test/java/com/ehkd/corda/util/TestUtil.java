package com.ehkd.corda.util;

import net.corda.core.concurrent.CordaFuture;

import java.time.Instant;
import java.time.ZonedDateTime;
import java.util.Random;
import java.util.concurrent.ExecutionException;

public class TestUtil {
    public static <T> T runFlow(TestParties tn, CordaFuture<T> future) throws ExecutionException, InterruptedException {
        tn.net.runNetwork();
        return future.get();
    }

    public static Instant createInstant(String timestamp) {
        return ZonedDateTime.parse(timestamp).toInstant();
    }

    public static String getRandomString(int maxLength) {
        String alphabet = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";
        int n = alphabet.length();

        Random random = new Random();
        StringBuilder sb = new StringBuilder();
        for( int i = 0; i < maxLength ; i++) {
            sb.append(alphabet.charAt(random.nextInt(n)));
        }
        return sb.toString();
    }
}
