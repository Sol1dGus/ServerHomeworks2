package org.example.attendance.repository;

import org.example.attendance.model.Lesson;
import org.example.attendance.model.StudentGroup;
import org.example.attendance.model.Subject;
import org.example.attendance.model.Teacher;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@ActiveProfiles("test")
class LessonRepositoryTest {

    @Autowired LessonRepository lessonRepository;
    @Autowired TeacherRepository teacherRepository;
    @Autowired SubjectRepository subjectRepository;
    @Autowired StudentGroupRepository groupRepository;

    @Test
    void saveAndFindById_works() {
        StudentGroup g = groupRepository.save(new StudentGroup(null, "ИКБО-LSN-01"));
        Teacher t = teacherRepository.save(new Teacher(null, "Преподаватель LSN"));
        Subject s = subjectRepository.save(new Subject(null, "Предмет LSN"));

        Lesson lesson = lessonRepository.save(new Lesson(null, t, s, g, LocalDate.of(2026, 5, 2), 2));

        Lesson loaded = lessonRepository.findById(lesson.getId()).orElseThrow();
        assertNotNull(loaded.getId());
        assertEquals(LocalDate.of(2026, 5, 2), loaded.getLessonDate());
        assertEquals(2, loaded.getPairNumber());
    }

    @Test
    void uniqueSlotConstraint_rejectsDuplicateLessonSlot() {
        StudentGroup g = groupRepository.save(new StudentGroup(null, "ИКБО-LSN-02"));
        Teacher t = teacherRepository.save(new Teacher(null, "Преподаватель SLOT"));
        Subject s = subjectRepository.save(new Subject(null, "Предмет SLOT"));

        LocalDate date = LocalDate.of(2026, 5, 2);
        int pair = 1;

        lessonRepository.saveAndFlush(new Lesson(null, t, s, g, date, pair));

        assertThrows(DataIntegrityViolationException.class,
                () -> lessonRepository.saveAndFlush(new Lesson(null, t, s, g, date, pair)));
    }
}
