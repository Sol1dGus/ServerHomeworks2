package org.example.attendance.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.AttendanceApplication;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(classes = AttendanceApplication.class)
@AutoConfigureMockMvc
@ActiveProfiles("test")
class SubjectControllerTest extends AuthenticatedControllerTestBase {

    @Test
    void createSubject_andGetAll_returnsStandardResponse() throws Exception {
        String token = registerAdminAndGetToken();

        var body = objectMapper.writeValueAsString(new CreateSubjectRequest("Математика"));

        mockMvc.perform(post("/api/subjects")
                        .header("Authorization", bearer(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").isNumber())
                .andExpect(jsonPath("$.data.name").value("Математика"));

        mockMvc.perform(get("/api/subjects")
                        .header("Authorization", bearer(token)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isArray());
    }

    public record CreateSubjectRequest(String name) {
    }
}

