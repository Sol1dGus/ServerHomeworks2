package org.example.attendance.repository;

import org.example.attendance.model.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@ActiveProfiles("test")
class AttendanceRepositoryTest {

    @Autowired AttendanceRepository attendanceRepository;
    @Autowired LessonRepository lessonRepository;
    @Autowired StudentRepository studentRepository;
    @Autowired StudentGroupRepository groupRepository;
    @Autowired TeacherRepository teacherRepository;
    @Autowired SubjectRepository subjectRepository;

    @Test
    void findAllByLessonId_deleteAllByLessonId_exists_checksWork() {
        StudentGroup g = groupRepository.save(new StudentGroup(null, "ИКБО-ATT-01"));
        Teacher t = teacherRepository.save(new Teacher(null, "Преподаватель REP"));
        Subject s = subjectRepository.save(new Subject(null, "Предмет REP"));

        Student st1 = studentRepository.save(new Student(null, "Студент 1", g));
        Student st2 = studentRepository.save(new Student(null, "Студент 2", g));

        Lesson lesson = lessonRepository.save(new Lesson(null, t, s, g, LocalDate.of(2026, 5, 1), 1));

        attendanceRepository.saveAll(List.of(
                new Attendance(null, lesson, st1, true),
                new Attendance(null, lesson, st2, true)
        ));

        assertTrue(attendanceRepository.existsByLessonId(lesson.getId()));
        assertTrue(attendanceRepository.existsByStudent_Id(st1.getId()));

        List<Attendance> all = attendanceRepository.findAllByLessonId(lesson.getId());
        assertEquals(2, all.size());

        attendanceRepository.deleteAllByLessonId(lesson.getId());

        assertFalse(attendanceRepository.existsByLessonId(lesson.getId()));
        assertFalse(attendanceRepository.existsByStudent_Id(st1.getId()));
        assertEquals(0, attendanceRepository.findAllByLessonId(lesson.getId()).size());
    }
}
