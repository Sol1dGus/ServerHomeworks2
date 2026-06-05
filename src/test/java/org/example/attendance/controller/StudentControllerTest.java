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
class StudentControllerTest extends AuthenticatedControllerTestBase {

    @Test
    void createStudent_withMissingGroup_returnsNotFoundStandardError() throws Exception {
        String token = registerAdminAndGetToken();

        var body = objectMapper.writeValueAsString(new CreateStudentRequest("Иванов Иван Иванович", 9999L));

        mockMvc.perform(post("/api/students")
                        .header("Authorization", bearer(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isNotFound())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.errorCode").value("NOT_FOUND"))
                .andExpect(jsonPath("$.errorMessage").exists());
    }

    public record CreateStudentRequest(String fullName, Long groupId) {
    }
}
