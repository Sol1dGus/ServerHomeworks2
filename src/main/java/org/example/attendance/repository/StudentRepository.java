package org.example.attendance.repository;

import org.example.attendance.model.Student;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;

public interface StudentRepository extends JpaRepository<Student, Long>, JpaSpecificationExecutor<Student> {
    boolean existsByFullNameIgnoreCase(String fullName);

    List<Student> findAllByGroupIdOrderByFullNameAsc(Long groupId);

    boolean existsByGroupId(Long groupId);
}
