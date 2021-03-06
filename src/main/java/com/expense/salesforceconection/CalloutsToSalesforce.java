package com.expense.salesforceconection;

import com.expense.BuildVars;
import com.expense.salesforceconection.mapper.SerializationToJSON;
import org.apache.http.HttpEntity;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;

import java.time.LocalDate;

import static com.expense.salesforceconection.RequestSenderUtil.sendPostRequest;

public class CalloutsToSalesforce {

    public static final String ERROR = "\"ERROR\"";

    private RequestParameters requestParameters;
    private boolean isNewCardProcessGoIn = false;
    private String amount;
    private String description;
    private LocalDate cardDate;
    private NewCardStage newCardStage = null;

    public boolean isCardReadyToSave() {
        return amount != null && description != null && cardDate != null;
    }

    public void setCardToNull() {
        amount = null;
        description = null;
        cardDate = null;
    }

    public CalloutsToSalesforce(RequestParameters requestParameters) {
        this.requestParameters = requestParameters;
    }

    public boolean isNewCardProcessGoIn() {
        return isNewCardProcessGoIn;
    }

    public void setNewCardProcessGoIn(boolean newCardProcessGoIn) {
        isNewCardProcessGoIn = newCardProcessGoIn;
    }

    public String getAmount() {
        return amount;
    }

    public void setAmount(String amount) {
        this.amount = amount;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public LocalDate getCardDate() {
        return cardDate;
    }

    public void setCardDate(LocalDate cardDate) {
        this.cardDate = cardDate;
    }

    public NewCardStage getNewCardStage() {
        return newCardStage;
    }

    public void setNewCardStage(NewCardStage newCardStage) {
        this.newCardStage = newCardStage;
    }

    public void closeNewCardProcess() {
        amount = null;
        description = null;
        cardDate = null;
        isNewCardProcessGoIn = false;
    }

    public String getBalance(String keeperId) {
        try {
            String url = requestParameters.getUrl() + BuildVars.REST_URL + "/balance";
            HttpEntity body = new StringEntity(SerializationToJSON.serializeOneParam(keeperId),
                    ContentType.APPLICATION_JSON);
            return sendPostRequest(url, body, requestParameters.getAccessToken());
        } catch (Exception ex) {
            ex.printStackTrace();
            return ERROR;
        }
    }

    public String createNew(String keeperId) {
        try {
            String url = requestParameters.getUrl() + BuildVars.REST_URL + "/card";
            HttpEntity body = new StringEntity(
                    SerializationToJSON.serializeNewExpenseData(keeperId, cardDate, amount, description),
                    ContentType.APPLICATION_JSON);
            return sendPostRequest(url, body, requestParameters.getAccessToken());
        } catch (Exception ex) {
            ex.printStackTrace();
            return ERROR;
        }
    }
}
