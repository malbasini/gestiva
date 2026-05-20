package com.gestiva.common.util;

import com.gestiva.common.exception.BusinessException;

import java.math.BigDecimal;

public final class NumberInputUtils {

    private NumberInputUtils() {
    }

    public static BigDecimal parseDecimal(String value, String fieldLabel) {
        if (value == null || value.trim().isEmpty()) {
            throw new BusinessException("Inserisci " + fieldLabel + ".");
        }

        String raw = value.trim();

        boolean hasComma = raw.contains(",");
        boolean hasDot = raw.contains(".");

        String normalized;

        if (hasComma && hasDot) {
            normalized = raw.replace(".", "").replace(",", ".");
        } else if (hasComma) {
            normalized = raw.replace(",", ".");
        } else {
            normalized = raw;
        }

        try {
            return new BigDecimal(normalized);
        } catch (NumberFormatException ex) {
            throw new BusinessException("Formato non valido per " + fieldLabel + ": " + value);
        }
    }
}