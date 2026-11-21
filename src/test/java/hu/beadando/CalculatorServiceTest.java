package hu.beadando;

import hu.beadando.service.CalculatorService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/** JUnit 5 egységtesztek a számológép szolgáltatásra. */
class CalculatorServiceTest {

    private CalculatorService calc;

    @BeforeEach
    void setUp() {
        calc = new CalculatorService();
    }

    private static BigDecimal n(String s) {
        return new BigDecimal(s);
    }

    @Test
    void addition() {
        assertEquals("5", CalculatorService.format(calc.add(n("2"), n("3"))));
    }

    @Test
    void additionOfDecimalsIsExact() {
        assertEquals("0.3", CalculatorService.format(calc.add(n("0.1"), n("0.2"))));
    }

    @Test
    void subtraction() {
        assertEquals("-4", CalculatorService.format(calc.subtract(n("3"), n("7"))));
    }

    @Test
    void multiplication() {
        assertEquals("12", CalculatorService.format(calc.multiply(n("3"), n("4"))));
    }

    @Test
    void multiplicationByZero() {
        assertEquals("0", CalculatorService.format(calc.multiply(n("123.45"), n("0"))));
    }

    @Test
    void division() {
        assertEquals("2.5", CalculatorService.format(calc.divide(n("5"), n("2"))));
    }

    @Test
    void divisionNonTerminating() {
        assertEquals("0.3333333333333333", CalculatorService.format(calc.divide(n("1"), n("3"))));
    }

    @Test
    void divisionByZeroThrows() {
        ArithmeticException ex = assertThrows(ArithmeticException.class, () -> calc.divide(n("1"), n("0")));
        assertEquals("Nullával nem lehet osztani.", ex.getMessage());
    }

    @ParameterizedTest
    @CsvSource({
            "add, 6, 3, 9",
            "sub, 6, 3, 3",
            "mul, 6, 3, 18",
            "div, 6, 3, 2",
            "add, -2, -3, -5",
            "mul, 0.5, 4, 2"
    })
    void calculateDispatchesOperation(String op, String a, String b, String expected) {
        assertEquals(expected, CalculatorService.format(calc.calculate(n(a), n(b), op)));
    }

    @Test
    void unknownOperationThrows() {
        assertThrows(IllegalArgumentException.class, () -> calc.calculate(n("1"), n("1"), "pow"));
    }

    @Test
    void nullOperationThrows() {
        assertThrows(IllegalArgumentException.class, () -> calc.calculate(n("1"), n("1"), null));
    }

    @Test
    void parseAcceptsDecimalComma() {
        assertEquals(0, n("1.5").compareTo(CalculatorService.parse("1,5")));
    }

    @Test
    void parseRejectsText() {
        assertThrows(IllegalArgumentException.class, () -> CalculatorService.parse("abc"));
    }

    @Test
    void parseRejectsBlank() {
        assertThrows(IllegalArgumentException.class, () -> CalculatorService.parse("  "));
    }
}
