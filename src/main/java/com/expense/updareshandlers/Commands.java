package com.expense.updareshandlers;

public interface Commands {
    String START = "/start";
    String STOP = "/stop";
    String CANCEL = "/cancel";
    String HELP = "/help";
    String BALANCE = "/balance";
    String NEW_EXPENSE_CARD = "/new_expense_card";
    String SET_DATE = "/set_date";
    String SET_AMOUNT = "/set_amount";
    String SET_DESCRIPTION = "/set_description";
    String TODAY = "/today";
    String CALENDAR = "/calendar";
    String CREATE_CARD = "/create_card";
    String DO_NOTHING = "/do_nothing";
    String NEXT_MONTH = "/next_month";
    String PREV_MONTH = "/prev_month";
}
