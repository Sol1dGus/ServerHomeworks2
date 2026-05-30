package org.example.attendance.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.AttendanceApplication;
import org.example.attendance.dto.ApiResponse;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(classes = AttendanceApplication.class)
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Import(GlobalExceptionHandlerDataIntegrityTest.TestErrorEndpointsConfig.class)
class GlobalExceptionHandlerDataIntegrityTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    @Test
    void deleteGroup_withExistingStudents_returnsConflictAndStandardError() throws Exception {
        long groupId = createGroup("ИКБО-DI-01");
        createStudent("Студент Для DI", groupId);

        mockMvc.perform(delete("/api/groups/" + groupId))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.errorCode").value("CONFLICT"));
    }

    @Test
    void deleteGroup_fkViolation_returnsDataIntegrityViolation_not500() throws Exception {
        long groupId = createGroup("ИКБО-DI-02");
        long teacherId = createTeacher("Препод DI");
        long subjectId = createSubject("Предмет DI");
        createLesson(teacherId, subjectId, groupId, "2026-05-20", 1);

        mockMvc.perform(delete("/api/groups/" + groupId))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    void notFound_returnsStandardError() throws Exception {
        mockMvc.perform(get("/api/teachers/999999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.errorCode").value("NOT_FOUND"))
                .andExpect(jsonPath("$.errorMessage").isNotEmpty());
    }

    @Test
    void conflict_returnsStandardError() throws Exception {
        createTeacher("Дубликат Для Ошибок");

        var body = objectMapper.writeValueAsString(new CreateTeacherRequest("Дубликат Для Ошибок"));
        mockMvc.perform(post("/api/teachers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.errorCode").value("CONFLICT"))
                .andExpect(jsonPath("$.errorMessage").isNotEmpty());
    }

    @Test
    void badRequest_customException_returnsStandardError() throws Exception {
        // from/to: to < from -> BadRequestException
        mockMvc.perform(get("/api/lessons")
                        .param("from", "2026-05-10")
                        .param("to", "2026-05-01"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.errorCode").value("BAD_REQUEST"))
                .andExpect(jsonPath("$.errorMessage").isNotEmpty());
    }

    @Test
    void validationError_returnsStandardErrorAndDetails() throws Exception {
        // name blank -> @NotBlank на DTO (валидация)
        var body = objectMapper.writeValueAsString(new CreateGroupRequest(""));
        mockMvc.perform(post("/api/groups")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.errorCode").value("VALIDATION_ERROR"))
                .andExpect(jsonPath("$.details").isArray());
    }

    @Test
    void typeMismatch_returnsBadRequest() throws Exception {
        // groupId должен быть числом
        mockMvc.perform(get("/api/students")
                        .param("groupId", "abc"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.errorCode").value("BAD_REQUEST"));
    }

    @Test
    void httpMessageNotReadable_returnsBadRequest() throws Exception {
        // Некорректный JSON (строка намеренно невалидна как JSON)
        String brokenJson = "{\"oops\":" + " }";

        mockMvc.perform(post("/api/teachers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(brokenJson))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.errorCode").value("BAD_REQUEST"));
    }

    @Test
    void dataIntegrityViolation_isHandled_not500() throws Exception {
        // Гарантируем, что обработчик DataIntegrityViolationException реально отрабатывает:
        // создаём "висящего" студента, который ссылается на несуществующую группу.
        mockMvc.perform(get("/api/test/throw-data-integrity"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.errorCode").value("DATA_INTEGRITY_VIOLATION"))
                .andExpect(jsonPath("$.errorMessage").isNotEmpty());
    }

    @Test
    void internalError_returnsStandardError() throws Exception {
        mockMvc.perform(get("/api/test/throw-internal"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.errorCode").value("INTERNAL_ERROR"))
                .andExpect(jsonPath("$.errorMessage").isNotEmpty());
    }

    private long createGroup(String name) throws Exception {
        var body = objectMapper.writeValueAsString(new CreateGroupRequest(name));
        MvcResult res = mockMvc.perform(post("/api/groups")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated())
                .andReturn();
        JsonNode json = objectMapper.readTree(res.getResponse().getContentAsString());
        return json.get("data").get("id").asLong();
    }

    private long createStudent(String fullName, long groupId) throws Exception {
        var body = objectMapper.writeValueAsString(new CreateStudentRequest(fullName, groupId));
        MvcResult res = mockMvc.perform(post("/api/students")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated())
                .andReturn();
        JsonNode json = objectMapper.readTree(res.getResponse().getContentAsString());
        return json.get("data").get("id").asLong();
    }

    private long createTeacher(String fullName) throws Exception {
        var body = objectMapper.writeValueAsString(new CreateTeacherRequest(fullName));
        MvcResult res = mockMvc.perform(post("/api/teachers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated())
                .andReturn();
        JsonNode json = objectMapper.readTree(res.getResponse().getContentAsString());
        return json.get("data").get("id").asLong();
    }

    private long createSubject(String name) throws Exception {
        var body = objectMapper.writeValueAsString(new CreateSubjectRequest(name));
        MvcResult res = mockMvc.perform(post("/api/subjects")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated())
                .andReturn();
        JsonNode json = objectMapper.readTree(res.getResponse().getContentAsString());
        return json.get("data").get("id").asLong();
    }

    private long createLesson(long teacherId, long subjectId, long groupId, String date, int pairNumber) throws Exception {
        var body = objectMapper.writeValueAsString(new CreateLessonRequest(teacherId, subjectId, groupId, date, pairNumber));
        MvcResult res = mockMvc.perform(post("/api/lessons")
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

    @TestConfiguration
    static class TestErrorEndpointsConfig {

        @Bean
        StudentJdbcTroublemaker studentJdbcTroublemaker(JdbcTemplate jdbcTemplate) {
            return new StudentJdbcTroublemaker(jdbcTemplate);
        }

        @Bean
        TestErrorController testErrorController(StudentJdbcTroublemaker troublemaker) {
            return new TestErrorController(troublemaker);
        }
    }

    @RestController
    static class TestErrorController {
        private final StudentJdbcTroublemaker troublemaker;

        TestErrorController(StudentJdbcTroublemaker troublemaker) {
            this.troublemaker = troublemaker;
        }

        @GetMapping("/api/test/throw-internal")
        public ApiResponse<Void> throwInternal() {
            throw new RuntimeException("boom");
        }

        @GetMapping("/api/test/throw-data-integrity")
        public ApiResponse<Void> throwDataIntegrity() {
            troublemaker.insertStudentWithBrokenFk();
            return ApiResponse.success(null);
        }
    }

    static class StudentJdbcTroublemaker {
        private final JdbcTemplate jdbcTemplate;

        StudentJdbcTroublemaker(JdbcTemplate jdbcTemplate) {
            this.jdbcTemplate = jdbcTemplate;
        }

        void insertStudentWithBrokenFk() {
            jdbcTemplate.update(
                    "insert into students(full_name, group_id) values (?, ?)",
                    "FK Test Student",
                    999999L
            );
        }
    }
}
