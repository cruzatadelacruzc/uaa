package com.example.uaa.config;

/**
 * Application constants.
 */
public final class Constants {

    // Regex for acceptable logins
    public static final String LOGIN_REGEX = "^[_.@A-Za-z0-9-]*$";

    public static final String SYSTEM_ACCOUNT = "system";
    public static final String DEFAULT_LANGUAGE = "es";
    public static final String ANONYMOUS_USER = "anonymoususer";

    public static final String PROFILE_DEV = "dev";
    public static final String PROFILE_PROD = "prod";

    public static final String ENTRY_WITHDRAW = "WITHDRAW";
    public static final String ENTRY_DEPOSIT = "DEPOSIT";
    public static final String PAYMENT_STATUS_CREATED = "created";
    public static final String PAYMENT_STATUS_APPROVED = "approved";
    public static final String CONCEPT_CREATED = "Created";
    public static final String CONCEPT_REMOVED = "Removed";
}
