package com.gestiva.documents.pdf;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

public final class PdfFormatUtils {

    private static final Locale ITALIAN_LOCALE = Locale.ITALY;
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final NumberFormat CURRENCY_FORMATTER = NumberFormat.getCurrencyInstance(ITALIAN_LOCALE);
    private static final NumberFormat DECIMAL_FORMATTER = NumberFormat.getNumberInstance(ITALIAN_LOCALE);

    static {
        DECIMAL_FORMATTER.setMinimumFractionDigits(2);
        DECIMAL_FORMATTER.setMaximumFractionDigits(2);
    }

    private PdfFormatUtils() {
    }

    public static String formatDate(LocalDate date) {
        return date == null ? "" : date.format(DATE_FORMATTER);
    }

    public static String formatMoney(BigDecimal value) {
        return value == null ? "" : CURRENCY_FORMATTER.format(value);
    }

    public static String formatDecimal(BigDecimal value) {
        return value == null ? "" : DECIMAL_FORMATTER.format(value);
    }
}