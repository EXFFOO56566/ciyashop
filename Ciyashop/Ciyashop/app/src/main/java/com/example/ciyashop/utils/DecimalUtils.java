package com.example.ciyashop.utils;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class DecimalUtils {
    public static double round(double value, int numberOfDigitsAfterDecimalPoint) {
        BigDecimal bigDecimal = new BigDecimal(value);
        bigDecimal = bigDecimal.setScale(numberOfDigitsAfterDecimalPoint,
                RoundingMode.HALF_UP);
        return bigDecimal.doubleValue();
    }
}
