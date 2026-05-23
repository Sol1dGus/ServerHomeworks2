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

import java.util.List;

@Service
@Transactional
public class StudentGroupService {
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
        if (groupRepository.existsByNameIgnoreCase(trimmed)) {
            throw new ConflictException("Группа с названием '" + trimmed + "' уже существует");
        }
        StudentGroup saved = groupRepository.save(new StudentGroup(null, trimmed));
        return toResponse(saved);
    }

    public StudentGroupResponse update(Long id, StudentGroupUpdateRequest req) {
        StudentGroup group = groupRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Группа с id=" + id + " не найдена"));

        String newName = req.getName().trim();
        if (!group.getName().equalsIgnoreCase(newName) && groupRepository.existsByNameIgnoreCase(newName)) {
            throw new ConflictException("Группа с названием '" + newName + "' уже существует");
        }

        group.setName(newName);
        return toResponse(group);
    }

    public void delete(Long id) {
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
    }

    private StudentGroupResponse toResponse(StudentGroup group) {
        return new StudentGroupResponse(group.getId(), group.getName());
    }
}
