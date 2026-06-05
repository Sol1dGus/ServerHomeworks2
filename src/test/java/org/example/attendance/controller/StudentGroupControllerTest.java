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

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(classes = AttendanceApplication.class)
@AutoConfigureMockMvc
@ActiveProfiles("test")
class StudentGroupControllerTest extends AuthenticatedControllerTestBase {

    @Test
    void createGroup_returnsCreatedAndStandardResponse() throws Exception {
        String token = registerAdminAndGetToken();

        var body = objectMapper.writeValueAsString(new CreateGroupRequest("ИКБО-01-21"));

        mockMvc.perform(post("/api/groups")
                        .header("Authorization", bearer(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").isNumber())
                .andExpect(jsonPath("$.data.name").value("ИКБО-01-21"));
    }

    public record CreateGroupRequest(String name) {
    }
}

