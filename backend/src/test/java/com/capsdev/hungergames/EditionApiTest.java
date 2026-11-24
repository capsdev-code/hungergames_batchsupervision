package com.capsdev.hungergames;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class EditionApiTest {

    @Autowired
    MockMvc mvc;

    @Test
    void listReturnsFourEditionsNewestFirst() throws Exception {
        mvc.perform(get("/api/editions"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(4))
                .andExpect(jsonPath("$[0].id").value(77))
                .andExpect(jsonPath("$[3].id").value(74));
    }

    @Test
    void completedEditionHasOneSurvivor() throws Exception {
        mvc.perform(get("/api/editions/74"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.summary.status").value("COMPLETED"))
                .andExpect(jsonPath("$.summary.tributeCount").value(24))
                .andExpect(jsonPath("$.summary.aliveCount").value(1));
    }

    @Test
    void functionalFailureReportsEmptyDistrictTwo() throws Exception {
        mvc.perform(get("/api/editions/76"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.summary.status").value("FAILED"))
                .andExpect(jsonPath("$.summary.tributeCount").value(22))
                .andExpect(jsonPath("$.errors[0].category").value("FUNCTIONAL"))
                .andExpect(jsonPath("$.errors[0].district").value(2))
                .andExpect(jsonPath("$.districts[1].district").value(2))
                .andExpect(jsonPath("$.districts[1].tributeCount").value(0))
                .andExpect(jsonPath("$.districts[1].expected").value(2));
    }

    @Test
    void technicalFailureHasTechnicalError() throws Exception {
        mvc.perform(get("/api/editions/77"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.errors[0].category").value("TECHNICAL"));
    }

    @Test
    void tributeEventsAreReturned() throws Exception {
        mvc.perform(get("/api/editions/75/tributes/7501/events"))
                .andExpect(status().isOk());
    }

    @Test
    void unknownEditionIs404() throws Exception {
        mvc.perform(get("/api/editions/999"))
                .andExpect(status().isNotFound());
    }
}
