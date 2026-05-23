package org.example.attendance.service;

import org.example.attendance.dto.subject.SubjectCreateRequest;
import org.example.attendance.dto.subject.SubjectResponse;
import org.example.attendance.dto.subject.SubjectUpdateRequest;
import org.example.attendance.exception.ConflictException;
import org.example.attendance.exception.NotFoundException;
import org.example.attendance.model.Subject;
import org.example.attendance.repository.LessonRepository;
import org.example.attendance.repository.SubjectRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class SubjectService {
    private final SubjectRepository subjectRepository;
    private final LessonRepository lessonRepository;

    public SubjectService(SubjectRepository subjectRepository, LessonRepository lessonRepository) {
        this.subjectRepository = subjectRepository;
        this.lessonRepository = lessonRepository;
    }

    @Transactional(readOnly = true)
    public List<SubjectResponse> getAll() {
        return subjectRepository.findAll().stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public SubjectResponse getById(Long id) {
        return toResponse(subjectRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Дисциплина с id=" + id + " не найдена")));
    }

    public SubjectResponse create(SubjectCreateRequest req) {
        String trimmed = req.getName() == null ? null : req.getName().trim();
        if (subjectRepository.existsByNameIgnoreCase(trimmed)) {
            throw new ConflictException("Дисциплина с названием '" + trimmed + "' уже существует");
        }
        Subject saved = subjectRepository.save(new Subject(null, trimmed));
        return toResponse(saved);
    }

    public SubjectResponse update(Long id, SubjectUpdateRequest req) {
        Subject subject = subjectRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Дисциплина с id=" + id + " не найдена"));

        String newName = req.getName().trim();
        if (!subject.getName().equalsIgnoreCase(newName) && subjectRepository.existsByNameIgnoreCase(newName)) {
            throw new ConflictException("Дисциплина с названием '" + newName + "' уже существует");
        }

        subject.setName(newName);
        return toResponse(subject);
    }

    public void delete(Long id) {
        Subject subject = subjectRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Дисциплина с id=" + id + " не найдена"));

        boolean hasLessons = lessonRepository.exists((root, query, cb) -> cb.equal(root.get("subject").get("id"), id));
        if (hasLessons) {
            throw new ConflictException("Нельзя удалить дисциплину: есть занятия");
        }

        subjectRepository.delete(subject);
    }

    private SubjectResponse toResponse(Subject subject) {
        return new SubjectResponse(subject.getId(), subject.getName());
    }
}

