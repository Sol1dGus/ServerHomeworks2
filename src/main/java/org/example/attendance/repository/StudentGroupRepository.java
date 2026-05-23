package org.example.attendance.repository;

import org.example.attendance.model.StudentGroup;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StudentGroupRepository extends JpaRepository<StudentGroup, Long> {
    boolean existsByNameIgnoreCase(String name);
}

