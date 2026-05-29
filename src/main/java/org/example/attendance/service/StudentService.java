package org.example.attendance.service;

import org.example.attendance.dto.student.StudentCreateRequest;
import org.example.attendance.dto.student.StudentResponse;
import org.example.attendance.dto.student.StudentUpdateRequest;
import org.example.attendance.exception.ConflictException;
import org.example.attendance.exception.NotFoundException;
import org.example.attendance.model.Student;
import org.example.attendance.model.StudentGroup;
import org.example.attendance.repository.AttendanceRepository;
import org.example.attendance.repository.StudentGroupRepository;
import org.example.attendance.repository.StudentRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

@Service
@Transactional
public class StudentService {
    private static final Logger log = LoggerFactory.getLogger(StudentService.class);

    private final StudentRepository studentRepository;
    private final StudentGroupRepository groupRepository;
    private final AttendanceRepository attendanceRepository;

    public StudentService(StudentRepository studentRepository,
                          StudentGroupRepository groupRepository,
                          AttendanceRepository attendanceRepository) {
        this.studentRepository = studentRepository;
        this.groupRepository = groupRepository;
        this.attendanceRepository = attendanceRepository;
    }

    @Transactional(readOnly = true)
    public StudentResponse getById(Long id) {
        return toResponse(studentRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Студент с id=" + id + " не найден")));
    }

    @Transactional(readOnly = true)
    public List<StudentResponse> getByGroup(Long groupId) {
        if (!groupRepository.existsById(groupId)) {
            throw new NotFoundException("Группа с id=" + groupId + " не найдена");
        }
        return studentRepository.findAllByGroupIdOrderByFullNameAsc(groupId).stream()
                .map(this::toResponse)
                .toList();
    }

    public StudentResponse create(StudentCreateRequest req) {
        String trimmedName = req.getFullName() == null ? null : req.getFullName().trim();
        log.info("Создание студента: fullName='{}', groupId={}", trimmedName, req.getGroupId());

        StudentGroup group = groupRepository.findById(req.getGroupId())
                .orElseThrow(() -> new NotFoundException("Группа с id=" + req.getGroupId() + " не найдена"));

        if (studentRepository.existsByFullNameIgnoreCase(trimmedName)) {
            throw new ConflictException("Студент с ФИО '" + trimmedName + "' уже существует");
        }

        Student saved = studentRepository.save(new Student(null, trimmedName, group));
        log.info("Студент создан: studentId={}, fullName='{}'", saved.getId(), saved.getFullName());
        return toResponse(saved);
    }

    public StudentResponse update(Long id, StudentUpdateRequest req) {
        log.info("Обновление студента: studentId={}, newFullName='{}', newGroupId={}", id, req.getFullName(), req.getGroupId());

        Student student = studentRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Студент с id=" + id + " не найден"));

        String newName = req.getFullName().trim();
        if (!student.getFullName().equalsIgnoreCase(newName) && studentRepository.existsByFullNameIgnoreCase(newName)) {
            throw new ConflictException("Студент с ФИО '" + newName + "' уже существует");
        }

        StudentGroup group = groupRepository.findById(req.getGroupId())
                .orElseThrow(() -> new NotFoundException("Группа с id=" + req.getGroupId() + " не найдена"));

        student.setFullName(newName);
        student.setGroup(group);
        log.info("Студент обновлён: studentId={}, fullName='{}', groupId={}", student.getId(), student.getFullName(), student.getGroup().getId());
        return toResponse(student);
    }

    public void delete(Long id) {
        log.info("Удаление студента: studentId={}", id);

        Student student = studentRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Студент с id=" + id + " не найден"));

        boolean hasAttendance = attendanceRepository.existsByStudent_Id(id);
        if (hasAttendance) {
            throw new ConflictException("Нельзя удалить студента: есть записи посещаемости");
        }

        studentRepository.delete(student);
        log.info("Студент удалён: studentId={}", id);
    }

    private StudentResponse toResponse(Student student) {
        return new StudentResponse(
                student.getId(),
                student.getFullName(),
                student.getGroup().getId(),
                student.getGroup().getName()
        );
    }
}
