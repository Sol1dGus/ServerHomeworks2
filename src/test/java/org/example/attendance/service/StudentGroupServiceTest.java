package org.example.attendance.service;

import org.example.AttendanceApplication;
import org.example.attendance.dto.group.StudentGroupCreateRequest;
import org.example.attendance.dto.student.StudentCreateRequest;
import org.example.attendance.exception.ConflictException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(classes = AttendanceApplication.class)
@ActiveProfiles("test")
class StudentGroupServiceTest extends BaseServiceTest {

    @Autowired
    StudentGroupService groupService;

    @Autowired
    StudentService studentService;

    @Test
    void createGroup_enforcesUniqueName_ignoreCase() {
        long g1 = createGroup("ИКБО-GRP-01");
        assertTrue(g1 > 0);

        assertThrows(ConflictException.class, () -> createGroup("икбо-grp-01"));
    }

    @Test
    void deleteGroup_isForbiddenIfHasStudents() {
        long groupId = createGroup("ИКБО-GRP-02");

        StudentCreateRequest s = new StudentCreateRequest();
        s.setFullName("Студент Для Группы");
        s.setGroupId(groupId);
        studentService.create(s);

        assertThrows(ConflictException.class, () -> groupService.delete(groupId));
    }

    private long createGroup(String name) {
        StudentGroupCreateRequest req = new StudentGroupCreateRequest();
        req.setName(name);
        return groupService.create(req).getId();
    }
}
