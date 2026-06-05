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

import java.time.LocalDate;
import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(classes = AttendanceApplication.class)
@AutoConfigureMockMvc
@ActiveProfiles("test")
class LessonControllerTest extends AuthenticatedControllerTestBase {

    @Test
    void createLesson_listByPeriod_andUpdateAttendance_works() throws Exception {
        String token = registerAdminAndGetToken();

        long groupId = createGroup("ИКБО-02-21", token);
        long s1 = createStudent("Иванов Иван Иванович", groupId, token);
        long s2 = createStudent("Сидоров Сидор Сидорович", groupId, token);
        long teacherId = createTeacher("Преподаватель Тестовый", token);
        long subjectId = createSubject("Физика", token);

        LocalDate date = LocalDate.of(2026, 5, 15);
        long lessonId = createLesson(teacherId, subjectId, groupId, date.toString(), 1, token);

        mockMvc.perform(get("/api/lessons")
                        .header("Authorization", bearer(token))
                        .param("from", date.minusDays(1).toString())
                        .param("to", date.plusDays(1).toString())
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.content").isArray())
                .andExpect(jsonPath("$.data.content[0].id").value(lessonId));

        // выставляем присутствие только для s1
        var updBody = objectMapper.writeValueAsString(new UpdateAttendanceRequest(List.of(s1)));
        mockMvc.perform(put("/api/lessons/" + lessonId + "/attendance")
                        .header("Authorization", bearer(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(updBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.attendance").isArray());

        // проверяем детали: s1 present=true, s2 present=false
        mockMvc.perform(get("/api/lessons/" + lessonId)
                        .header("Authorization", bearer(token)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.attendance").isArray())
                // Проверяем студента s1 ("Иванов") - он должен быть present=true
                .andExpect(jsonPath("$.data.attendance[0].studentId").value(s1))
                .andExpect(jsonPath("$.data.attendance[0].present").value(true))
                // Проверяем студента s2 ("Сидоров") - он должен быть present=false
                .andExpect(jsonPath("$.data.attendance[1].studentId").value(s2))
                .andExpect(jsonPath("$.data.attendance[1].present").value(false));
    }

    private long createGroup(String name, String token) throws Exception {
        var body = objectMapper.writeValueAsString(new CreateGroupRequest(name));
        MvcResult res = mockMvc.perform(post("/api/groups")
                        .header("Authorization", bearer(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated())
                .andReturn();
        JsonNode json = objectMapper.readTree(res.getResponse().getContentAsString());
        return json.get("data").get("id").asLong();
    }

    private long createStudent(String fullName, long groupId, String token) throws Exception {
        var body = objectMapper.writeValueAsString(new CreateStudentRequest(fullName, groupId));
        MvcResult res = mockMvc.perform(post("/api/students")
                        .header("Authorization", bearer(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated())
                .andReturn();
        JsonNode json = objectMapper.readTree(res.getResponse().getContentAsString());
        return json.get("data").get("id").asLong();
    }

    private long createTeacher(String fullName, String token) throws Exception {
        var body = objectMapper.writeValueAsString(new CreateTeacherRequest(fullName));
        MvcResult res = mockMvc.perform(post("/api/teachers")
                        .header("Authorization", bearer(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated())
                .andReturn();
        JsonNode json = objectMapper.readTree(res.getResponse().getContentAsString());
        return json.get("data").get("id").asLong();
    }

    private long createSubject(String name, String token) throws Exception {
        var body = objectMapper.writeValueAsString(new CreateSubjectRequest(name));
        MvcResult res = mockMvc.perform(post("/api/subjects")
                        .header("Authorization", bearer(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated())
                .andReturn();
        JsonNode json = objectMapper.readTree(res.getResponse().getContentAsString());
        return json.get("data").get("id").asLong();
    }

    private long createLesson(long teacherId, long subjectId, long groupId, String date, int pairNumber, String token) throws Exception {
        var body = objectMapper.writeValueAsString(new CreateLessonRequest(teacherId, subjectId, groupId, date, pairNumber));
        MvcResult res = mockMvc.perform(post("/api/lessons")
                        .header("Authorization", bearer(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated())
                .andReturn();
        JsonNode json = objectMapper.readTree(res.getResponse().getContentAsString());
        return json.get("data").get("id").asLong();
    }

    public record CreateGroupRequest(String name) {
    }

    public record CreateStudentRequest(String fullName, Long groupId) {
    }

    public record CreateTeacherRequest(String fullName) {
    }

    public record CreateSubjectRequest(String name) {
    }

    public record CreateLessonRequest(Long teacherId, Long subjectId, Long groupId, String date, Integer pairNumber) {
    }

    public record UpdateAttendanceRequest(List<Long> presentStudentIds) {
    }
}
