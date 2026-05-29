package org.example.attendance.service;

import org.example.attendance.dto.group.StudentGroupCreateRequest;
import org.example.attendance.dto.group.StudentGroupResponse;
import org.example.attendance.dto.group.StudentGroupUpdateRequest;
import org.example.attendance.exception.ConflictException;
import org.example.attendance.exception.NotFoundException;
import org.example.attendance.model.StudentGroup;
import org.example.attendance.repository.LessonRepository;
import org.example.attendance.repository.StudentGroupRepository;
import org.example.attendance.repository.StudentRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

@Service
@Transactional
public class StudentGroupService {
    private static final Logger log = LoggerFactory.getLogger(StudentGroupService.class);

    private final StudentGroupRepository groupRepository;
    private final StudentRepository studentRepository;
    private final LessonRepository lessonRepository;

    public StudentGroupService(StudentGroupRepository groupRepository,
                               StudentRepository studentRepository,
                               LessonRepository lessonRepository) {
        this.groupRepository = groupRepository;
        this.studentRepository = studentRepository;
        this.lessonRepository = lessonRepository;
    }

    @Transactional(readOnly = true)
    public List<StudentGroupResponse> getAll() {
        return groupRepository.findAll().stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public StudentGroupResponse getById(Long id) {
        return toResponse(groupRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Группа с id=" + id + " не найдена")));
    }

    public StudentGroupResponse create(StudentGroupCreateRequest req) {
        String trimmed = req.getName() == null ? null : req.getName().trim();
        log.info("Создание группы: name='{}'", trimmed);

        if (groupRepository.existsByNameIgnoreCase(trimmed)) {
            throw new ConflictException("Группа с названием '" + trimmed + "' уже существует");
        }
        StudentGroup saved = groupRepository.save(new StudentGroup(null, trimmed));
        log.info("Группа создана: groupId={}, name='{}'", saved.getId(), saved.getName());
        return toResponse(saved);
    }

    public StudentGroupResponse update(Long id, StudentGroupUpdateRequest req) {
        log.info("Обновление группы: groupId={}, newName='{}'", id, req.getName());

        StudentGroup group = groupRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Группа с id=" + id + " не найдена"));

        String newName = req.getName().trim();
        if (!group.getName().equalsIgnoreCase(newName) && groupRepository.existsByNameIgnoreCase(newName)) {
            throw new ConflictException("Группа с названием '" + newName + "' уже существует");
        }

        group.setName(newName);
        log.info("Группа обновлена: groupId={}, name='{}'", group.getId(), group.getName());
        return toResponse(group);
    }

    public void delete(Long id) {
        log.info("Удаление группы: groupId={}", id);

        StudentGroup group = groupRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Группа с id=" + id + " не найдена"));

        if (studentRepository.existsByGroupId(id)) {
            throw new ConflictException("Нельзя удалить группу: есть студенты");
        }
        boolean hasLessons = lessonRepository.exists((root, query, cb) -> cb.equal(root.get("group").get("id"), id));
        if (hasLessons) {
            throw new ConflictException("Нельзя удалить группу: есть занятия");
        }

        groupRepository.delete(group);
        log.info("Группа удалена: groupId={}", id);
    }

    private StudentGroupResponse toResponse(StudentGroup group) {
        return new StudentGroupResponse(group.getId(), group.getName());
    }
}
