package org.example.attendance.service;

import org.example.AttendanceApplication;
import org.example.attendance.dto.teacher.TeacherCreateRequest;
import org.example.attendance.dto.teacher.TeacherUpdateRequest;
import org.example.attendance.exception.ConflictException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(classes = AttendanceApplication.class)
@ActiveProfiles("test")
class TeacherServiceTest extends BaseServiceTest {

    @Autowired
    TeacherService teacherService;

    @Test
    void createAndUpdateTeacher_enforcesUniqueFullName_ignoreCase() {
        long id = createTeacher("Александров Преподаватель");
        assertTrue(id > 0);

        assertThrows(ConflictException.class, () -> createTeacher("александров преподаватель"));

        TeacherUpdateRequest upd = new TeacherUpdateRequest();
        upd.setFullName("Борисов Преподаватель");
        var updated = teacherService.update(id, upd);
        assertEquals("Борисов Преподаватель", updated.getFullName());

        long otherId = createTeacher("Васильев Преподаватель");
        TeacherUpdateRequest conflict = new TeacherUpdateRequest();
        conflict.setFullName("борисов преподаватель");
        assertThrows(ConflictException.class, () -> teacherService.update(otherId, conflict));
    }

    private long createTeacher(String fullName) {
        TeacherCreateRequest req = new TeacherCreateRequest();
        req.setFullName(fullName);
        return teacherService.create(req).getId();
    }
}
