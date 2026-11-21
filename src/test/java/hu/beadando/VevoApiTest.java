package hu.beadando;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/** A React kliens REST API-ja: létrehozás, lekérdezés, módosítás, törlés. */
@SpringBootTest(properties = {"spring.datasource.url=jdbc:h2:mem:apitest", "app.seed=false"})
@AutoConfigureMockMvc
class VevoApiTest {

    private static final String JSON = "{\"nev\":\"Teszt Elek\",\"email\":\"t@e.hu\",\"varos\":\"Szarvas\",\"telefon\":\"123\"}";

    @Autowired
    private MockMvc mockMvc;

    private long create() throws Exception {
        MvcResult r = mockMvc.perform(post("/api/vevok").contentType(MediaType.APPLICATION_JSON).content(JSON))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andReturn();
        Matcher m = Pattern.compile("\"id\":(\\d+)").matcher(r.getResponse().getContentAsString());
        m.find();
        return Long.parseLong(m.group(1));
    }

    @Test
    void createAndRead() throws Exception {
        long id = create();
        mockMvc.perform(get("/api/vevok/" + id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nev").value("Teszt Elek"));
    }

    @Test
    void listContainsCreated() throws Exception {
        create();
        mockMvc.perform(get("/api/vevok"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Teszt Elek")));
    }

    @Test
    void update() throws Exception {
        long id = create();
        mockMvc.perform(put("/api/vevok/" + id).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nev\":\"Módosított\",\"email\":\"\",\"varos\":\"\",\"telefon\":\"\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nev").value("Módosított"));
    }

    @Test
    void deleteThenNotFound() throws Exception {
        long id = create();
        mockMvc.perform(delete("/api/vevok/" + id)).andExpect(status().isNoContent());
        mockMvc.perform(get("/api/vevok/" + id)).andExpect(status().isNotFound());
    }

    @Test
    void blankNameIsRejected() throws Exception {
        mockMvc.perform(post("/api/vevok").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nev\":\"\",\"email\":\"x@y.hu\"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void unknownIdIsNotFound() throws Exception {
        mockMvc.perform(delete("/api/vevok/999999")).andExpect(status().isNotFound());
    }
}
