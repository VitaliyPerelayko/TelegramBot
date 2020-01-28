package com.expense.salesforceconection;

import com.expense.BuildVars;
import com.expense.salesforceconection.mapper.SerializationToJSON;
import org.apache.http.HttpEntity;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;

import java.io.IOException;

import static com.expense.salesforceconection.RequestSenderUtil.sendPostRequest;

public class LoginToExpenseApp {

    private RequestParameters requestParameters;
    private boolean inProcess = true;
    private LoginStage stage = LoginStage.USERNAME;
    private String validUsername;
    private String keeperId;
    private String office;
    private boolean isAdmin;

    public LoginToExpenseApp(RequestParameters requestParameters) {
        this.requestParameters = requestParameters;
    }

    public boolean isInProcess() {
        return inProcess;
    }

    public void setInProcess(boolean inProcess) {
        this.inProcess = inProcess;
    }

    public LoginStage getStage() {
        return stage;
    }

    public void setStage(LoginStage stage) {
        this.stage = stage;
    }

    public String getValidUsername() {
        return validUsername;
    }

    public void setValidUsername(String validUsername) {
        this.validUsername = validUsername;
    }

    public String getKeeperId() {
        return keeperId;
    }

    public void setKeeperId(String keeperId) {
        this.keeperId = keeperId;
    }

    public String getOffice() {
        return office;
    }

    public void setOffice(String office) {
        this.office = office;
    }

    public boolean isAdmin() {
        return isAdmin;
    }

    public void setAdmin(boolean admin) {
        isAdmin = admin;
    }

    public String setUsername(String username) {
        String url = requestParameters.getUrl() + BuildVars.REST_URL + "/username";
        HttpEntity body = new StringEntity(SerializationToJSON.serializeOneParam(username),
                ContentType.APPLICATION_JSON);
        try {
            return sendPostRequest(url, body, requestParameters.getAccessToken());
        } catch (IOException e) {
            e.printStackTrace();
        }
        return CalloutsToSalesforce.ERROR;
    }

    public String setPassword(String password) {
        String url = requestParameters.getUrl() + BuildVars.REST_URL + "/password";
        HttpEntity body = new StringEntity(SerializationToJSON.serializeUsernameAndPassword(validUsername, password),
                ContentType.APPLICATION_JSON);
        try {
            return sendPostRequest(url, body, requestParameters.getAccessToken());
        } catch (IOException e) {
            e.printStackTrace();
        }
        return CalloutsToSalesforce.ERROR;
    }

    public static void main(String[] args) {
        RequestParameters rp = Login.getToken();
        if (rp != null) {
            LoginToExpenseApp loginToExpenseApp = new LoginToExpenseApp(rp);
            //loginToExpenseApp.setPassword("ant@ant.com", "123");
        }
    }
}
