package org.example.attendance.service;

import org.example.AttendanceApplication;
import org.example.attendance.dto.subject.SubjectCreateRequest;
import org.example.attendance.dto.subject.SubjectUpdateRequest;
import org.example.attendance.exception.ConflictException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(classes = AttendanceApplication.class)
@ActiveProfiles("test")
class SubjectServiceTest extends BaseServiceTest {

    @Autowired
    SubjectService subjectService;

    @Test
    void createAndUpdateSubject_enforcesUniqueName_ignoreCase() {
        long id = createSubject("Информатика");
        assertTrue(id > 0);

        assertThrows(ConflictException.class, () -> createSubject("информатика"));

        SubjectUpdateRequest upd = new SubjectUpdateRequest();
        upd.setName("Программирование");
        var updated = subjectService.update(id, upd);
        assertEquals("Программирование", updated.getName());

        long otherId = createSubject("Математика-2");
        SubjectUpdateRequest conflict = new SubjectUpdateRequest();
        conflict.setName("программирование");
        assertThrows(ConflictException.class, () -> subjectService.update(otherId, conflict));
    }

    private long createSubject(String name) {
        SubjectCreateRequest req = new SubjectCreateRequest();
        req.setName(name);
        return subjectService.create(req).getId();
    }
}
