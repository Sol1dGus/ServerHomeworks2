package org.example.attendance.repository;

import org.example.attendance.model.Teacher;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TeacherRepository extends JpaRepository<Teacher, Long> {
    boolean existsByFullNameIgnoreCase(String fullName);
}