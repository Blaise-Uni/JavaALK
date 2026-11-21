package hu.beadando.service;

import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.MathContext;

/** Négyműveletes számológép (a Teszt menü alkalmazása). */
@Service
public class CalculatorService {

    public BigDecimal add(BigDecimal a, BigDecimal b) {
        return a.add(b);
    }

    public BigDecimal subtract(BigDecimal a, BigDecimal b) {
        return a.subtract(b);
    }

    public BigDecimal multiply(BigDecimal a, BigDecimal b) {
        return a.multiply(b);
    }

    public BigDecimal divide(BigDecimal a, BigDecimal b) {
        if (b.signum() == 0) {
            throw new ArithmeticException("Nullával nem lehet osztani.");
        }
        return a.divide(b, MathContext.DECIMAL64);
    }

    /** op értékei: add, sub, mul, div. */
    public BigDecimal calculate(BigDecimal a, BigDecimal b, String op) {
        if (op == null) {
            throw new IllegalArgumentException("Hiányzik a művelet.");
        }
        return switch (op) {
            case "add" -> add(a, b);
            case "sub" -> subtract(a, b);
            case "mul" -> multiply(a, b);
            case "div" -> divide(a, b);
            default -> throw new IllegalArgumentException("Ismeretlen művelet: " + op);
        };
    }

    /** Szám szöveggé alakítása felesleges nullák nélkül (tizedesvessző is elfogadott bemenetként). */
    public static BigDecimal parse(String text) {
        if (text == null || text.isBlank()) {
            throw new IllegalArgumentException("Hiányzó szám.");
        }
        try {
            return new BigDecimal(text.trim().replace(',', '.'));
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Érvénytelen szám: " + text);
        }
    }

    public static String format(BigDecimal value) {
        return value.signum() == 0 ? "0" : value.stripTrailingZeros().toPlainString();
    }
}
