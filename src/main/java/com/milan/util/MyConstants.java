package com.milan.util;

public class MyConstants {

    public static final String EMAIL_REGEX = "^[A-Za-z0-9+_.-]+@gmail\\.com$";

    public static final String MOBILE_REGEX = "^[6-9]\\d{9}$";

    public static final String ZIP_CODE_REGEX = "^\\d{5}(-\\d{4})?$";

    public static final String ROLE_ADMIN="hasRole('ADMIN')";
    public static final String ROLE_ADMIN_USER="hasAnyRole('USER','ADMIN')";
    public static final String ROLE_USER="hasRole('USER')";

    public static final String DEFAULT_PAGE_NO="0";
    public static final String DEFAULT_PAGE_SIZE="10";

    public static final int FIRST_NAME_MAX_LENGTH = 50;
    public static final int LAST_NAME_MAX_LENGTH = 50;
    public static final int PASSWORD_MIN_LENGTH = 8;

    public static final int REFUND_WINDOW_DAYS = 7;
}
