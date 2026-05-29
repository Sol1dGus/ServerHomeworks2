package org.example.attendance.repository;

import org.example.attendance.model.Student;
import org.example.attendance.model.StudentGroup;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@ActiveProfiles("test")
class StudentRepositoryTest {

    @Autowired StudentRepository studentRepository;
    @Autowired StudentGroupRepository groupRepository;

    @Test
    void existsByFullNameIgnoreCase_works() {
        StudentGroup g = groupRepository.save(new StudentGroup(null, "ИКБО-ST-01"));
        studentRepository.save(new Student(null, "Петров Петр", g));

        assertTrue(studentRepository.existsByFullNameIgnoreCase("петров петр"));
        assertTrue(studentRepository.existsByFullNameIgnoreCase("ПЕТРОВ ПЕТР"));
        assertFalse(studentRepository.existsByFullNameIgnoreCase("Сидоров Сидор"));
    }

    @Test
    void findAllByGroupIdOrderByFullNameAsc_ordersAndFilters() {
        StudentGroup g1 = groupRepository.save(new StudentGroup(null, "ИКБО-ST-02"));
        StudentGroup g2 = groupRepository.save(new StudentGroup(null, "ИКБО-ST-03"));

        studentRepository.saveAll(List.of(
                new Student(null, "Яковлев Яков", g1),
                new Student(null, "Александров Александр", g1),
                new Student(null, "Борисов Борис", g1),
                new Student(null, "Вне группы", g2)
        ));

        List<Student> result = studentRepository.findAllByGroupIdOrderByFullNameAsc(g1.getId());
        assertEquals(3, result.size());
        assertEquals("Александров Александр", result.get(0).getFullName());
        assertEquals("Борисов Борис", result.get(1).getFullName());
        assertEquals("Яковлев Яков", result.get(2).getFullName());
    }

    @Test
    void existsByGroupId_works() {
        StudentGroup empty = groupRepository.save(new StudentGroup(null, "ИКБО-ST-04"));
        StudentGroup nonEmpty = groupRepository.save(new StudentGroup(null, "ИКБО-ST-05"));

        assertFalse(studentRepository.existsByGroupId(empty.getId()));

        studentRepository.save(new Student(null, "Студент в группе", nonEmpty));
        assertTrue(studentRepository.existsByGroupId(nonEmpty.getId()));
    }
}

