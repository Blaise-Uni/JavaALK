package hu.beadando;

import hu.beadando.controller.CalculatorController;
import hu.beadando.service.CalculatorService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

/** MockMvc tesztek: a Teszt menü űrlapja HTTP szinten. */
@WebMvcTest(CalculatorController.class)
@Import(CalculatorService.class)
class CalculatorControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void formIsShown() throws Exception {
        mockMvc.perform(get("/teszt"))
                .andExpect(status().isOk())
                .andExpect(view().name("teszt"))
                .andExpect(content().string(containsString("Számológép")));
    }

    @Test
    void addition() throws Exception {
        mockMvc.perform(post("/teszt").param("a", "2").param("b", "3").param("op", "add"))
                .andExpect(status().isOk())
                .andExpect(model().attribute("result", "5"));
    }

    @Test
    void subtraction() throws Exception {
        mockMvc.perform(post("/teszt").param("a", "2").param("b", "5").param("op", "sub"))
                .andExpect(model().attribute("result", "-3"));
    }

    @Test
    void multiplication() throws Exception {
        mockMvc.perform(post("/teszt").param("a", "4").param("b", "2.5").param("op", "mul"))
                .andExpect(model().attribute("result", "10"));
    }

    @Test
    void division() throws Exception {
        mockMvc.perform(post("/teszt").param("a", "7").param("b", "2").param("op", "div"))
                .andExpect(model().attribute("result", "3.5"));
    }

    @Test
    void decimalCommaIsAccepted() throws Exception {
        mockMvc.perform(post("/teszt").param("a", "1,5").param("b", "1,5").param("op", "add"))
                .andExpect(model().attribute("result", "3"));
    }

    @Test
    void divisionByZeroShowsError() throws Exception {
        mockMvc.perform(post("/teszt").param("a", "1").param("b", "0").param("op", "div"))
                .andExpect(status().isOk())
                .andExpect(model().attributeDoesNotExist("result"))
                .andExpect(model().attribute("error", "Nullával nem lehet osztani."))
                .andExpect(content().string(containsString("Nullával nem lehet osztani.")));
    }

    @Test
    void invalidNumberShowsError() throws Exception {
        mockMvc.perform(post("/teszt").param("a", "abc").param("b", "1").param("op", "add"))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists("error"));
    }

    @Test
    void unknownOperationShowsError() throws Exception {
        mockMvc.perform(post("/teszt").param("a", "1").param("b", "1").param("op", "pow"))
                .andExpect(model().attributeExists("error"));
    }

    @Test
    void missingParameterIsBadRequest() throws Exception {
        mockMvc.perform(post("/teszt").param("a", "1").param("op", "add"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void enteredValuesAreKeptInTheForm() throws Exception {
        mockMvc.perform(post("/teszt").param("a", "8").param("b", "2").param("op", "div"))
                .andExpect(model().attribute("a", "8"))
                .andExpect(model().attribute("b", "2"))
                .andExpect(model().attribute("op", "div"));
    }
}
