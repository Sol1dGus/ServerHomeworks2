package org.example.attendance.service;

import org.example.attendance.dto.teacher.TeacherCreateRequest;
import org.example.attendance.dto.teacher.TeacherResponse;
import org.example.attendance.dto.teacher.TeacherUpdateRequest;
import org.example.attendance.exception.ConflictException;
import org.example.attendance.exception.NotFoundException;
import org.example.attendance.model.Teacher;
import org.example.attendance.repository.LessonRepository;
import org.example.attendance.repository.TeacherRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class TeacherService {
    private final TeacherRepository teacherRepository;
    private final LessonRepository lessonRepository;

    public TeacherService(TeacherRepository teacherRepository, LessonRepository lessonRepository) {
        this.teacherRepository = teacherRepository;
        this.lessonRepository = lessonRepository;
    }

    @Transactional(readOnly = true)
    public TeacherResponse getById(Long id) {
        return toResponse(teacherRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Преподаватель с id=" + id + " не найден")));
    }

    @Transactional(readOnly = true)
    public Page<TeacherResponse> getAll(Pageable pageable) {
        return teacherRepository.findAll(pageable).map(this::toResponse);
    }

    public TeacherResponse create(TeacherCreateRequest req) {
        String trimmed = req.getFullName() == null ? null : req.getFullName().trim();
        if (teacherRepository.existsByFullNameIgnoreCase(trimmed)) {
            throw new ConflictException("Преподаватель с ФИО '" + trimmed + "' уже существует");
        }
        Teacher saved = teacherRepository.save(new Teacher(null, trimmed));
        return toResponse(saved);
    }

    public TeacherResponse update(Long id, TeacherUpdateRequest req) {
        Teacher teacher = teacherRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Преподаватель с id=" + id + " не найден"));

        String newName = req.getFullName().trim();
        if (!teacher.getFullName().equalsIgnoreCase(newName) && teacherRepository.existsByFullNameIgnoreCase(newName)) {
            throw new ConflictException("Преподаватель с ФИО '" + newName + "' уже существует");
        }

        teacher.setFullName(newName);
        return toResponse(teacher);
    }

    public void delete(Long id) {
        Teacher teacher = teacherRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Преподаватель с id=" + id + " не найден"));

        boolean hasLessons = lessonRepository.exists((root, query, cb) -> cb.equal(root.get("teacher").get("id"), id));
        if (hasLessons) {
            throw new ConflictException("Нельзя удалить преподавателя: есть занятия");
        }

        teacherRepository.delete(teacher);
    }

    private TeacherResponse toResponse(Teacher teacher) {
        return new TeacherResponse(teacher.getId(), teacher.getFullName());
    }
}

