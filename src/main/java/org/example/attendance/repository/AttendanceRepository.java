package org.example.attendance.repository;

import org.example.attendance.model.Attendance;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AttendanceRepository extends JpaRepository<Attendance, Long> {
    List<Attendance> findAllByLessonId(Long lessonId);

    void deleteAllByLessonId(Long lessonId);

    boolean existsByLessonId(Long lessonId);

    boolean existsByStudent_Id(Long studentId);
}
