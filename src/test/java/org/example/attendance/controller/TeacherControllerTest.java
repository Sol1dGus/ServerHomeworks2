package org.example.attendance.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.AttendanceApplication;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(classes = AttendanceApplication.class)
@AutoConfigureMockMvc
@ActiveProfiles("test")
class TeacherControllerTest extends AuthenticatedControllerTestBase {

    @Test
    void teachers_crud_andPaging_works() throws Exception {
        String token = registerAdminAndGetToken();

        long t1 = createTeacher("Иванов Преподаватель", token);
        long t2 = createTeacher("Петров Преподаватель", token);

        mockMvc.perform(get("/api/teachers/" + t1)
                        .header("Authorization", bearer(token)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(t1))
                .andExpect(jsonPath("$.data.fullName").value("Иванов Преподаватель"));

        mockMvc.perform(get("/api/teachers")
                        .header("Authorization", bearer(token))
                        .param("page", "0")
                        .param("size", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.content").isArray())
                .andExpect(jsonPath("$.data.content.length()").value(1));

        var updBody = objectMapper.writeValueAsString(new UpdateTeacherRequest("Сидоров Преподаватель"));
        mockMvc.perform(put("/api/teachers/" + t2)
                        .header("Authorization", bearer(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(updBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(t2))
                .andExpect(jsonPath("$.data.fullName").value("Сидоров Преподаватель"));

        mockMvc.perform(delete("/api/teachers/" + t2)
                        .header("Authorization", bearer(token)))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/teachers/" + t2)
                        .header("Authorization", bearer(token)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.errorCode").value("NOT_FOUND"));
    }

    @Test
    void createTeacher_duplicateFullName_returnsConflict() throws Exception {
        String token = registerAdminAndGetToken();

        createTeacher("Дубликат Тестовый", token);

        var body = objectMapper.writeValueAsString(new CreateTeacherRequest("Дубликат Тестовый"));
        mockMvc.perform(post("/api/teachers")
                        .header("Authorization", bearer(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.errorCode").value("CONFLICT"));
    }

    private long createTeacher(String fullName, String token) throws Exception {
        var body = objectMapper.writeValueAsString(new CreateTeacherRequest(fullName));
        MvcResult res = mockMvc.perform(post("/api/teachers")
                        .header("Authorization", bearer(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andReturn();
        JsonNode json = objectMapper.readTree(res.getResponse().getContentAsString());
        return json.get("data").get("id").asLong();
    }

    public record CreateTeacherRequest(String fullName) {
    }

    public record UpdateTeacherRequest(String fullName) {
    }
}

