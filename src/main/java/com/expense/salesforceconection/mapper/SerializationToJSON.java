package com.expense.salesforceconection.mapper;

import java.time.LocalDate;

public class SerializationToJSON {

    public static String serializeOneParam(String username) {
        return String.format("{\"body\": \"%s\"}", username);
    }

    public static String serializeUsernameAndPassword(String username, String password) {
        return String.format("{\"body\": \"%1$s # %2$s\"}", username, password);
    }

    public static String serializeNewExpenseData(String keeperId, LocalDate date, String amount, String description) {
        return String.format("{\"body\": \"%1$s # %2$s # %3$s # %4$s\"}",keeperId, date.toString(), amount, description);
    }

    public static void main(String[] args) {
        System.out.println(serializeNewExpenseData("bla@bal.com", LocalDate.now(), "bla", "Ferrari"));
    }
}
