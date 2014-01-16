package com.cookietest;

import retrofit.RequestInterceptor;

public class AuthenticatedRequestInterceptor implements RequestInterceptor {

    private Cookies mTokens;

    public AuthenticatedRequestInterceptor(Cookies tokens) {
        mTokens = tokens;
    }

    @Override
    public void intercept(RequestFacade request) {
        request.addHeader("Cookie", mTokens.toString());
    }
}
