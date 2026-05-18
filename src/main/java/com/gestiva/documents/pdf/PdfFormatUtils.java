package com.gestiva.documents.pdf;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

public final class PdfFormatUtils {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy");


    private PdfFormatUtils() {
    }

    public static String formatDate(LocalDate date) {
        return date == null ? "" : date.format(DATE_FORMATTER);
    }

    public static String formatDecimal(BigDecimal value) {
        return formatDecimal(value, 2);
    }

    public static String formatDecimal(BigDecimal value, int scale) {
        if (value == null) {
            return "";
        }

        BigDecimal scaled = value.setScale(scale, RoundingMode.HALF_UP);

        DecimalFormatSymbols symbols = new DecimalFormatSymbols(Locale.ITALY);
        symbols.setDecimalSeparator(',');
        symbols.setGroupingSeparator('.');

        StringBuilder pattern = new StringBuilder("#,##0");
        if (scale > 0) {
            pattern.append(".");
            pattern.append("0".repeat(scale));
        }

        DecimalFormat decimalFormat = new DecimalFormat(pattern.toString(), symbols);
        decimalFormat.setMinimumFractionDigits(scale);
        decimalFormat.setMaximumFractionDigits(scale);

        return decimalFormat.format(scaled);
    }

    public static String formatMoney(BigDecimal value) {
        return formatDecimal(value, 2);
    }

    public static String formatDecimalTrimmed(BigDecimal value, int maxScale) {
        if (value == null) {
            return "";
        }

        BigDecimal scaled = value.setScale(maxScale, RoundingMode.HALF_UP).stripTrailingZeros();

        DecimalFormatSymbols symbols = new DecimalFormatSymbols(Locale.ITALY);
        symbols.setDecimalSeparator(',');
        symbols.setGroupingSeparator('.');

        DecimalFormat decimalFormat = new DecimalFormat("#,##0.################", symbols);
        decimalFormat.setMaximumFractionDigits(maxScale);
        decimalFormat.setMinimumFractionDigits(0);

        return decimalFormat.format(scaled);
    }
}