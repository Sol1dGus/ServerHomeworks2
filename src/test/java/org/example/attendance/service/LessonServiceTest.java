package org.example.attendance.service;

import org.example.AttendanceApplication;
import org.example.attendance.dto.group.StudentGroupCreateRequest;
import org.example.attendance.dto.lesson.LessonAttendanceUpdateRequest;
import org.example.attendance.dto.lesson.LessonCreateRequest;
import org.example.attendance.dto.lesson.LessonUpdateRequest;
import org.example.attendance.dto.student.StudentCreateRequest;
import org.example.attendance.dto.subject.SubjectCreateRequest;
import org.example.attendance.dto.teacher.TeacherCreateRequest;
import org.example.attendance.exception.BadRequestException;
import org.example.attendance.exception.ConflictException;
import org.example.attendance.repository.AttendanceRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(classes = AttendanceApplication.class)
@ActiveProfiles("test")
class LessonServiceTest extends BaseServiceTest {

    @Autowired LessonService lessonService;
    @Autowired StudentGroupService groupService;
    @Autowired StudentService studentService;
    @Autowired TeacherService teacherService;
    @Autowired SubjectService subjectService;
    @Autowired AttendanceRepository attendanceRepository;

    @Test
    void listValidation_toBeforeFrom_throwsBadRequest() {
        assertThrows(BadRequestException.class,
                () -> lessonService.getList(LocalDate.of(2026, 1, 2), LocalDate.of(2026, 1, 1), null, null, PageRequest.of(0, 10)));
    }

    @Test
    void createLesson_uniqueSlot_conflictOnDuplicate() {
        long g = createGroup("ИКБО-LES-01");
        long t = createTeacher("Тестовый Препод");
        long s = createSubject("Тестовая Дисциплина");

        LessonCreateRequest req = new LessonCreateRequest();
        req.setGroupId(g);
        req.setTeacherId(t);
        req.setSubjectId(s);
        req.setDate(LocalDate.of(2026, 5, 10));
        req.setPairNumber(1);

        long lessonId = lessonService.create(req).getId();
        assertTrue(lessonId > 0);

        assertThrows(ConflictException.class, () -> lessonService.create(req));
    }

    @Test
    void updateLesson_clearsAttendance_whenGroupChanged() {
        long g1 = createGroup("ИКБО-LES-02");
        long g2 = createGroup("ИКБО-LES-03");
        long st1 = createStudent("Студент Один", g1);
        createStudent("Студент Два", g1);
        long t = createTeacher("Препод Для Занятия");
        long subj = createSubject("Предмет Для Занятия");

        LessonCreateRequest create = new LessonCreateRequest();
        create.setGroupId(g1);
        create.setTeacherId(t);
        create.setSubjectId(subj);
        create.setDate(LocalDate.of(2026, 5, 11));
        create.setPairNumber(2);
        long lessonId = lessonService.create(create).getId();

        lessonService.updateAttendance(lessonId, new LessonAttendanceUpdateRequest(List.of(st1)));
        assertTrue(attendanceRepository.existsByLessonId(lessonId));

        LessonUpdateRequest upd = new LessonUpdateRequest();
        upd.setGroupId(g2);
        upd.setTeacherId(t);
        upd.setSubjectId(subj);
        upd.setDate(LocalDate.of(2026, 5, 11));
        upd.setPairNumber(2);

        lessonService.update(lessonId, upd);
        assertFalse(attendanceRepository.existsByLessonId(lessonId));

        var details = lessonService.getByIdWithAttendance(lessonId);
        assertEquals(g2, details.getGroupId());
        assertEquals(0, details.getAttendance().size(), "в новой группе нет студентов, значит attendance пуст");
    }

    @Test
    void updateAttendance_rejectsStudentsNotFromGroup() {
        long g1 = createGroup("ИКБО-LES-04");
        long g2 = createGroup("ИКБО-LES-05");
        long stOtherGroup = createStudent("Левый Студент", g2);
        createStudent("Норм Студент", g1);
        long t = createTeacher("Препод Для Attendance");
        long subj = createSubject("Предмет Для Attendance");

        LessonCreateRequest create = new LessonCreateRequest();
        create.setGroupId(g1);
        create.setTeacherId(t);
        create.setSubjectId(subj);
        create.setDate(LocalDate.of(2026, 5, 12));
        create.setPairNumber(3);
        long lessonId = lessonService.create(create).getId();

        assertThrows(BadRequestException.class,
                () -> lessonService.updateAttendance(lessonId, new LessonAttendanceUpdateRequest(List.of(stOtherGroup))));
    }

    private long createGroup(String name) {
        StudentGroupCreateRequest req = new StudentGroupCreateRequest();
        req.setName(name);
        return groupService.create(req).getId();
    }

    private long createStudent(String fullName, long groupId) {
        StudentCreateRequest req = new StudentCreateRequest();
        req.setFullName(fullName);
        req.setGroupId(groupId);
        return studentService.create(req).getId();
    }

    private long createTeacher(String fullName) {
        TeacherCreateRequest req = new TeacherCreateRequest();
        req.setFullName(fullName);
        return teacherService.create(req).getId();
    }

    private long createSubject(String name) {
        SubjectCreateRequest req = new SubjectCreateRequest();
        req.setName(name);
        return subjectService.create(req).getId();
    }
}
