package com.augusto.twofactorauthenticaton.security;

public class SecurityConstants {
    public static final String SECRET = "fda5f2765iwkpf2dbc48b65056d42fe3";
    public static final long EXPIRATION_TIME = 432000000; // 5 days
    public static final String TOKEN_PREFIX = "Bearer ";
    public static final String HEADER_STRING = "Authorization";
    public static final String HEADER_OTP = "HEADER_OTP";
}
