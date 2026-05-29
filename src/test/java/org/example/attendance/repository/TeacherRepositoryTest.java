package org.example.attendance.repository;

import org.example.attendance.model.Teacher;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@ActiveProfiles("test")
class TeacherRepositoryTest {

    @Autowired TeacherRepository teacherRepository;

    @Test
    void existsByFullNameIgnoreCase_works() {
        teacherRepository.save(new Teacher(null, "Иванов Иван"));

        assertTrue(teacherRepository.existsByFullNameIgnoreCase("иванов иван"));
        assertTrue(teacherRepository.existsByFullNameIgnoreCase("ИВАНОВ ИВАН"));
        assertFalse(teacherRepository.existsByFullNameIgnoreCase("Петров Петр"));
    }
}

