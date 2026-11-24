package com.capsdev.hungergames;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(properties = "app.batch.tick-ms=5")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
class BatchSimulatorTest {

    @Autowired
    MockMvc mvc;

    @Autowired
    ObjectMapper mapper;

    @Test
    void normalLaunchCompletesWithOneSurvivor() throws Exception {
        long id = launch("{\"scenario\":\"NORMAL\"}");
        JsonNode detail = awaitEnd(id);
        assertEquals("COMPLETED", detail.at("/summary/status").asText());
        assertEquals(24, detail.at("/summary/tributeCount").asInt());
        assertEquals(1, detail.at("/summary/aliveCount").asInt());
        assertEquals(0, detail.at("/errors").size());
    }

    @Test
    void missingDistrictFailsFunctionally() throws Exception {
        long id = launch("{\"scenario\":\"MISSING_DISTRICT\",\"missingDistrict\":5}");
        JsonNode detail = awaitEnd(id);
        assertEquals("FAILED", detail.at("/summary/status").asText());
        assertEquals("FUNCTIONAL", detail.at("/errors/0/category").asText());
        assertEquals(5, detail.at("/errors/0/district").asInt());
        assertEquals("FAILED", detail.at("/phases/0/status").asText());
        assertEquals("PENDING", detail.at("/phases/1/status").asText());
    }

    @Test
    void technicalFailureFailsInArena() throws Exception {
        long id = launch("{\"scenario\":\"TECHNICAL_FAILURE\"}");
        JsonNode detail = awaitEnd(id);
        assertEquals("FAILED", detail.at("/summary/status").asText());
        assertEquals("TECHNICAL", detail.at("/errors/0/category").asText());
        assertEquals("ARENE", detail.at("/errors/0/phase").asText());
        assertEquals(8, detail.at("/summary/eliminatedCount").asInt());
    }

    @Test
    void launchWithoutBodyDefaultsToNormal() throws Exception {
        long id = launch(null);
        assertEquals("COMPLETED", awaitEnd(id).at("/summary/status").asText());
    }

    @Test
    void relaunchOfFailedEditionCreatesNewEdition() throws Exception {
        String body = mvc.perform(post("/api/editions/76/relaunch"))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        long id = mapper.readTree(body).get("id").asLong();
        assertNotEquals(76, id);
        assertEquals("COMPLETED", awaitEnd(id).at("/summary/status").asText());
    }

    @Test
    void relaunchOfCompletedEditionIsRejected() throws Exception {
        mvc.perform(post("/api/editions/74/relaunch")).andExpect(status().isConflict());
    }

    @Test
    void invalidDistrictIsRejected() throws Exception {
        mvc.perform(post("/api/editions").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"scenario\":\"MISSING_DISTRICT\",\"missingDistrict\":13}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void secondLaunchWhileRunningIsRejected() throws Exception {
        // tick de 5 ms : on enchaîne 2 lancements immédiatement, le second tombe pendant l'exécution du premier
        long id = launch("{\"scenario\":\"NORMAL\"}");
        mvc.perform(post("/api/editions")).andExpect(status().isConflict());
        awaitEnd(id);
    }

    private long launch(String body) throws Exception {
        var request = post("/api/editions");
        if (body != null) {
            request = request.contentType(MediaType.APPLICATION_JSON).content(body);
        }
        String json = mvc.perform(request)
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        return mapper.readTree(json).get("id").asLong();
    }

    private JsonNode awaitEnd(long id) throws Exception {
        for (int i = 0; i < 200; i++) {
            String json = mvc.perform(get("/api/editions/" + id))
                    .andReturn().getResponse().getContentAsString();
            JsonNode detail = mapper.readTree(json);
            if (!"STARTED".equals(detail.at("/summary/status").asText())) {
                Thread.sleep(50); // laisse le thread libérer le verrou de lancement
                return detail;
            }
            Thread.sleep(50);
        }
        throw new AssertionError("L'édition " + id + " ne s'est pas terminée à temps");
    }
}
