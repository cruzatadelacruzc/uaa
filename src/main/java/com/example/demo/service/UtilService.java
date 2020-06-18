package com.example.demo.service;

import org.apache.commons.lang.RandomStringUtils;

import java.security.SecureRandom;

public final class UtilService {

    private static final SecureRandom SECURE_RANDOM;

    static {
        SECURE_RANDOM = new SecureRandom();
        SECURE_RANDOM.nextBytes(new byte[64]);
    }

    /**
     * Generator of Random Strings
     * @return Random Strings
     */
    public static String generateRandomAlphanumericString() {
        return RandomStringUtils.random(20, 0, 0, true, true, null, SECURE_RANDOM);
    }
}
