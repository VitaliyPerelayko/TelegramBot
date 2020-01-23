package com.expense.salesforceconection;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;

import java.io.IOException;

public class RequestSenderUtil {

    private static HttpClient httpclient = HttpClientBuilder.create().build();

    public static String sendPostRequest(String url, HttpEntity body, String accessToken) {
        HttpPost http = new HttpPost(url);
        http.addHeader("Authorization", "Bearer " + accessToken);
        http.setEntity(body);
        try {
            HttpResponse response = httpclient.execute(http);
            System.out.println("setBody response status: " + response.getStatusLine());
            String result = EntityUtils.toString(response.getEntity());
            System.out.println(result);
            return result;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
}
