package com.expense.salesforceconection;


import com.expense.BuildVars;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.io.IOException;

public class Login {

    public static RequestParameters getToken() {

        RequestParameters rp = new RequestParameters();

        HttpClient httpclient = HttpClientBuilder.create().build();

        // Assemble the login request URL
        String loginURL = BuildVars.LOGIN_URL +
                BuildVars.GRANTSERVICE +
                "&client_id=" + BuildVars.CONSUMER_KEY +
                "&client_secret=" + BuildVars.CONSUMER_SECRET +
                "&username=" + BuildVars.USERNAME +
                "&password=" + BuildVars.PASSWORD;

        // Login requests must be POSTs
        HttpPost httpPost = new HttpPost(loginURL);

        try {
            // Execute the login POST request
            HttpResponse response = httpclient.execute(httpPost);
            final int statusCode = response.getStatusLine().getStatusCode();
            if (statusCode != HttpStatus.SC_OK) {
                System.out.println("Error authenticating to Force.com: " + statusCode);
                // Error is in EntityUtils.toString(response.getEntity())
            } else {
                String result = EntityUtils.toString(response.getEntity());
                JSONObject jsonObject = (JSONObject) new JSONTokener(result).nextValue();
                String loginAccessToken = jsonObject.getString("access_token");
                String loginInstanceUrl = jsonObject.getString("instance_url");
                rp.setUrl(loginInstanceUrl);
                rp.setAccessToken(loginAccessToken);

                System.out.println(response.getStatusLine());
                System.out.println("Successful login");
                System.out.println("  instance URL: " + loginInstanceUrl);
                System.out.println("  access token/session ID: " + loginAccessToken);

                // release connection
                httpPost.releaseConnection();
                return rp;
            }
        } catch (IOException cpException) {
            cpException.printStackTrace();
        }
        return null;
    }
}