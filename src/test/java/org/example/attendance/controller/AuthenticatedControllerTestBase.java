package org.example.attendance.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.concurrent.atomic.AtomicInteger;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

public abstract class AuthenticatedControllerTestBase {

    private static final AtomicInteger COUNTER = new AtomicInteger(0);

    @Autowired
    protected MockMvc mockMvc;

    @Autowired
    protected ObjectMapper objectMapper;
    protected String registerAdminAndGetToken() throws Exception {
        String username = "admin_" + COUNTER.incrementAndGet();
        String body = objectMapper.writeValueAsString(
                new RegisterRequest(username, "admin123", "ADMIN"));
        MvcResult res = mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andReturn();
        JsonNode json = objectMapper.readTree(res.getResponse().getContentAsString());
        return json.get("data").get("accessToken").asText();
    }

    protected String registerTeacherAndGetToken(long teacherId) throws Exception {
        String username = "teacher_" + teacherId + "_" + COUNTER.incrementAndGet();
        String body = objectMapper.writeValueAsString(
                new RegisterRequest(username, "teacher123", "TEACHER", null, teacherId));
        MvcResult res = mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andReturn();
        JsonNode json = objectMapper.readTree(res.getResponse().getContentAsString());
        return json.get("data").get("accessToken").asText();
    }

    protected String registerStudentAndGetToken(long studentId) throws Exception {
        String username = "student_" + studentId + "_" + COUNTER.incrementAndGet();
        String body = objectMapper.writeValueAsString(
                new RegisterRequest(username, "student123", "STUDENT", studentId, null));
        MvcResult res = mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andReturn();
        JsonNode json = objectMapper.readTree(res.getResponse().getContentAsString());
        return json.get("data").get("accessToken").asText();
    }

    protected String login(String username, String password) throws Exception {
        String body = objectMapper.writeValueAsString(
                new LoginRequest(username, password));
        MvcResult res = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andReturn();
        JsonNode json = objectMapper.readTree(res.getResponse().getContentAsString());
        return json.get("data").get("accessToken").asText();
    }

    protected String bearer(String token) {
        return "Bearer " + token;
    }

    public record RegisterRequest(String username, String password, String role, Long studentId, Long teacherId) {
        public RegisterRequest(String username, String password, String role) {
            this(username, password, role, null, null);
        }
    }

    public record LoginRequest(String username, String password) {
    }
}
