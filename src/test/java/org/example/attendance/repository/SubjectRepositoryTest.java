package org.example.attendance.repository;

import org.example.attendance.model.Subject;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@ActiveProfiles("test")
class SubjectRepositoryTest {

    @Autowired SubjectRepository subjectRepository;

    @Test
    void existsByNameIgnoreCase_works() {
        subjectRepository.save(new Subject(null, "Математика"));

        assertTrue(subjectRepository.existsByNameIgnoreCase("математика"));
        assertTrue(subjectRepository.existsByNameIgnoreCase("МАТЕМАТИКА"));
        assertFalse(subjectRepository.existsByNameIgnoreCase("Физика"));
    }
}

