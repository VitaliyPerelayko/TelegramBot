package com.expense.salesforceconection;

import org.apache.http.entity.mime.Header;

public class RequestParameters {

    private String url;
    private String accessToken;

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getAccessToken() {
        return accessToken;
    }

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }
}
