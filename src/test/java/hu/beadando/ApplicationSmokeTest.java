package hu.beadando;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** A teljes alkalmazás elindul, és a hálózatot nem igénylő oldalak megjelennek. */
@SpringBootTest(properties = {"spring.datasource.url=jdbc:h2:mem:smoke", "app.seed=false"})
@AutoConfigureMockMvc
class ApplicationSmokeTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void homePageLoadsWithFooter() throws Exception {
        mockMvc.perform(get("/"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Készítették")));
    }

    @Test
    void testPageIsInTheMenu() throws Exception {
        mockMvc.perform(get("/"))
                .andExpect(content().string(containsString("/teszt")));
    }

}
