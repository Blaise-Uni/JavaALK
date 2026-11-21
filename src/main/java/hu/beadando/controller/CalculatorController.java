package hu.beadando.controller;

import hu.beadando.service.CalculatorService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.math.BigDecimal;

@Controller
public class CalculatorController {

    private final CalculatorService calc;

    public CalculatorController(CalculatorService calc) {
        this.calc = calc;
    }

    @GetMapping("/teszt")
    public String form(Model model) {
        model.addAttribute("op", "add");
        return "teszt";
    }

    @PostMapping("/teszt")
    public String calculate(@RequestParam String a, @RequestParam String b,
                            @RequestParam String op, Model model) {
        model.addAttribute("a", a);
        model.addAttribute("b", b);
        model.addAttribute("op", op);
        try {
            BigDecimal result = calc.calculate(CalculatorService.parse(a), CalculatorService.parse(b), op);
            model.addAttribute("result", CalculatorService.format(result));
        } catch (IllegalArgumentException | ArithmeticException e) {
            model.addAttribute("error", e.getMessage());
        }
        return "teszt";
    }
}
