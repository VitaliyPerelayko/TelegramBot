package com.expense.inlinekeyboard;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Calendar {
    // days[i] = number of days in month i
    private static final int[] days = {
            0, 31, 28, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31
    };

    /**
     * generate table for month by weekdays from Monday to Sunday
     *
     * @param month num of month (1 - 12)
     * @param year  num of year
     * @return Table
     */
    public static List<String[]> generateMonthTable(int year, int month) {
        List<String[]> monthTable = new ArrayList<>(6);
        LocalDate calendar = LocalDate.of(year, month, 1);

        // check for leap year
        if (month == 2 && calendar.isLeapYear()) {
            days[month] = 29;
        }
        // starting day Mon = 0 ... Sun = 6
        int d = calendar.getDayOfWeek().getValue() - 1;

        String[] firstLine = new String[7];
        // first week dates
        for (int i = 0; i < d; i++) {
            firstLine[i] = (" ");
        }
        for (int i = 1; i <= 7 - d; i++) {
            firstLine[d + i - 1] = String.valueOf(i);
        }
        monthTable.add(firstLine);

        int dayCounter = 7 - d + 1;

        while (dayCounter <= days[month]) {
            String[] line = new String[7];
            for (int i = 0; i < 7; i++) {
                if (dayCounter > days[month]){
                    line[i] = " ";
                } else {
                    line[i] = String.valueOf(dayCounter);
                }
                dayCounter++;
            }
            monthTable.add(line);
        }
        return monthTable;
    }

    public static void main(String[] args) {
        System.out.println(LocalDate.of(2019, 11, 1).getDayOfWeek());
        generateMonthTable(2019, 8).forEach(strings ->
                System.out.println(Arrays.toString(strings))
        );
    }
}
