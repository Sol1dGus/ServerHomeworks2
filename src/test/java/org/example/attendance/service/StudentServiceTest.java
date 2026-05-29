package org.example.attendance.service;

import org.example.AttendanceApplication;
import org.example.attendance.dto.group.StudentGroupCreateRequest;
import org.example.attendance.dto.student.StudentCreateRequest;
import org.example.attendance.dto.student.StudentUpdateRequest;
import org.example.attendance.exception.ConflictException;
import org.example.attendance.exception.NotFoundException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(classes = AttendanceApplication.class)
@ActiveProfiles("test")
class StudentServiceTest extends BaseServiceTest {

    @Autowired
    StudentService studentService;

    @Autowired
    StudentGroupService groupService;

    @Test
    void createStudent_requiresExistingGroup_andEnforcesUniqueFullName() {
        StudentCreateRequest req = new StudentCreateRequest();
        req.setFullName("Иванов Иван");
        req.setGroupId(9999L);
        assertThrows(NotFoundException.class, () -> studentService.create(req));

        long groupId = createGroup("ИКБО-STU-01");

        req.setGroupId(groupId);
        var created = studentService.create(req);
        assertNotNull(created.getId());
        assertEquals("Иванов Иван", created.getFullName());
        assertEquals(groupId, created.getGroupId());

        StudentCreateRequest dup = new StudentCreateRequest();
        dup.setFullName("иванов иван");
        dup.setGroupId(groupId);
        assertThrows(ConflictException.class, () -> studentService.create(dup));
    }

    @Test
    void updateStudent_changesNameAndGroup_andEnforcesUniqueness() {
        long g1 = createGroup("ИКБО-STU-02");
        long g2 = createGroup("ИКБО-STU-03");

        StudentCreateRequest c1 = new StudentCreateRequest();
        c1.setFullName("Петров Петр");
        c1.setGroupId(g1);
        var s1 = studentService.create(c1);

        StudentCreateRequest c2 = new StudentCreateRequest();
        c2.setFullName("Сидоров Сидор");
        c2.setGroupId(g1);
        var s2 = studentService.create(c2);

        StudentUpdateRequest upd = new StudentUpdateRequest();
        upd.setFullName("Иванов Иван");
        upd.setGroupId(g2);

        var updated = studentService.update(s1.getId(), upd);
        assertEquals("Иванов Иван", updated.getFullName());
        assertEquals(g2, updated.getGroupId());

        StudentUpdateRequest conflict = new StudentUpdateRequest();
        conflict.setFullName("Сидоров Сидор");
        conflict.setGroupId(g2);
        assertThrows(ConflictException.class, () -> studentService.update(updated.getId(), conflict));

        StudentUpdateRequest badGroup = new StudentUpdateRequest();
        badGroup.setFullName("Новый");
        badGroup.setGroupId(99999L);
        assertThrows(NotFoundException.class, () -> studentService.update(s2.getId(), badGroup));
    }

    private long createGroup(String name) {
        StudentGroupCreateRequest req = new StudentGroupCreateRequest();
        req.setName(name);
        return groupService.create(req).getId();
    }
}
