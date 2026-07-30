package com.mk.jwtauth.service;

public enum TokenType{
    TOKEN_TEMPORARY,
    TOKEN_LOGIN;

    public static final String TOKEN_TYPE_KEY = "token-type";

}
