package com.cookietest;

import java.net.HttpCookie;
import java.util.List;

public class Tokens {
    private String sitecoreUserticket;
    private String ASPXAUTH;
    private String ASP_NET_SessionId;
    private String CSRFCOOKIE;

    public Tokens(List<HttpCookie> cookies){
        for (HttpCookie cookie: cookies){
            if (cookie.getName().equals("ASP.NET_SessionId")) {
                ASP_NET_SessionId = cookie.getValue();
                continue;
            }
            if (cookie.getName().equals(".ASPXAUTH")) {
                ASPXAUTH = cookie.getValue();
                continue;
            }
            if (cookie.getName().equals("__CSRFCOOKIE")) {
                CSRFCOOKIE = cookie.getValue();
                continue;
            }
            if (cookie.getName().equals("sitecore_userticket")) {
                sitecoreUserticket = cookie.getValue();
            }
        }
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("sitecore_userticket").append("=").append(sitecoreUserticket).append(";");
        builder.append(".ASPXAUTH").append("=").append(ASPXAUTH).append(";");
        builder.append("ASP.NET_SessionId").append("=").append(ASP_NET_SessionId).append(";");
        builder.append("__CSRFCOOKIE").append("=").append(CSRFCOOKIE).append(";");
        return builder.toString();
    }

    public void setSitecoreUserticket(String sitecoreUserticket) {
        this.sitecoreUserticket = sitecoreUserticket;
    }

    public void setASPXAUTH(String ASPXAUTH) {
        this.ASPXAUTH = ASPXAUTH;
    }

    public void setASP_NET_SessionId(String ASP_NET_SessionId) {
        this.ASP_NET_SessionId = ASP_NET_SessionId;
    }

    public void setCSRFCOOKIE(String CSRFCOOKIE) {
        this.CSRFCOOKIE = CSRFCOOKIE;
    }

    public String getSitecoreUserticket() {
        return sitecoreUserticket;
    }

    public String getASPXAUTH() {
        return ASPXAUTH;
    }

    public String getASP_NET_SessionId() {
        return ASP_NET_SessionId;
    }

    public String getCSRFCOOKIE() {
        return CSRFCOOKIE;
    }
}
