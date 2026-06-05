package org.example.attendance.service;

import org.example.attendance.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;

public abstract class BaseServiceTest {

    @Autowired
    AttendanceRepository attendanceRepository;

    @Autowired
    LessonRepository lessonRepository;

    @Autowired
    StudentRepository studentRepository;

    @Autowired
    StudentGroupRepository studentGroupRepository;

    @Autowired
    SubjectRepository subjectRepository;

    @Autowired
    TeacherRepository teacherRepository;

    @Autowired
    UserRepository userRepository;

    @BeforeEach
    void cleanDb() {
        userRepository.deleteAll();
        attendanceRepository.deleteAll();
        lessonRepository.deleteAll();
        studentRepository.deleteAll();
        studentGroupRepository.deleteAll();
        subjectRepository.deleteAll();
        teacherRepository.deleteAll();
    }
}

