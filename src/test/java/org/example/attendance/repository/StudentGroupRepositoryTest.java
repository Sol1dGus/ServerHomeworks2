package org.example.attendance.repository;

import org.example.attendance.model.StudentGroup;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@ActiveProfiles("test")
class StudentGroupRepositoryTest {

    @Autowired StudentGroupRepository studentGroupRepository;

    @Test
    void existsByNameIgnoreCase_works() {
        studentGroupRepository.save(new StudentGroup(null, "ИКБО-01-21"));

        assertTrue(studentGroupRepository.existsByNameIgnoreCase("икбо-01-21"));
        assertTrue(studentGroupRepository.existsByNameIgnoreCase("ИКБО-01-21"));
        assertFalse(studentGroupRepository.existsByNameIgnoreCase("ИКБО-02-21"));
    }
}

