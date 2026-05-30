package org.example.attendance.service;

import org.example.attendance.dto.lesson.*;
import org.example.attendance.exception.BadRequestException;
import org.example.attendance.exception.ConflictException;
import org.example.attendance.exception.NotFoundException;
import org.example.attendance.model.*;
import org.example.attendance.repository.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional
public class LessonService {
    private static final Logger log = LoggerFactory.getLogger(LessonService.class);

    private final LessonRepository lessonRepository;
    private final TeacherRepository teacherRepository;
    private final SubjectRepository subjectRepository;
    private final StudentGroupRepository groupRepository;
    private final StudentRepository studentRepository;
    private final AttendanceRepository attendanceRepository;

    public LessonService(LessonRepository lessonRepository,
                         TeacherRepository teacherRepository,
                         SubjectRepository subjectRepository,
                         StudentGroupRepository groupRepository,
                         StudentRepository studentRepository,
                         AttendanceRepository attendanceRepository) {
        this.lessonRepository = lessonRepository;
        this.teacherRepository = teacherRepository;
        this.subjectRepository = subjectRepository;
        this.groupRepository = groupRepository;
        this.studentRepository = studentRepository;
        this.attendanceRepository = attendanceRepository;
    }

    @Transactional(readOnly = true)
    public LessonDetailsResponse getByIdWithAttendance(Long id) {
        Lesson lesson = lessonRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Занятие с id=" + id + " не найдено"));

        List<Student> groupStudents = studentRepository.findAllByGroupIdOrderByFullNameAsc(lesson.getGroup().getId());
        Map<Long, Boolean> presentMap = attendanceRepository.findAllByLessonId(id).stream()
                .collect(Collectors.toMap(a -> a.getStudent().getId(), Attendance::isPresent));

        List<LessonDetailsResponse.AttendanceItem> items = groupStudents.stream()
                .map(s -> new LessonDetailsResponse.AttendanceItem(
                        s.getId(),
                        s.getFullName(),
                        presentMap.getOrDefault(s.getId(), false)
                ))
                .toList();

        return new LessonDetailsResponse(
                lesson.getId(),
                lesson.getTeacher().getId(),
                lesson.getTeacher().getFullName(),
                lesson.getSubject().getId(),
                lesson.getSubject().getName(),
                lesson.getGroup().getId(),
                lesson.getGroup().getName(),
                lesson.getLessonDate(),
                lesson.getPairNumber(),
                items
        );
    }

    @Transactional(readOnly = true)
    public Page<LessonListItemResponse> getList(LocalDate from, LocalDate to, Long groupId, Long teacherId, Pageable pageable) {
        if (from == null || to == null) {
            throw new BadRequestException("Параметры from и to обязательны");
        }
        if (to.isBefore(from)) {
            throw new BadRequestException("Параметр to не может быть меньше from");
        }

        Specification<Lesson> spec = (root, query, cb) -> cb.between(root.get("lessonDate"), from, to);
        if (groupId != null) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("group").get("id"), groupId));
        }
        if (teacherId != null) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("teacher").get("id"), teacherId));
        }

        return lessonRepository.findAll(spec, pageable).map(this::toListItem);
    }

    public LessonDetailsResponse create(LessonCreateRequest req) {
        log.info("Создание занятия: teacherId={}, subjectId={}, groupId={}, date={}, pairNumber={}",
                req.getTeacherId(), req.getSubjectId(), req.getGroupId(), req.getDate(), req.getPairNumber());

        Lesson lesson = new Lesson();
        applyUpdate(lesson, req.getTeacherId(), req.getSubjectId(), req.getGroupId(), req.getDate(), req.getPairNumber());
        Lesson saved;
        try {
            saved = lessonRepository.saveAndFlush(lesson);
        } catch (DataIntegrityViolationException ex) {
            throw new ConflictException("Занятие в этот слот уже существует (преподаватель/группа/дата/пара)");
        }

        log.info("Занятие создано: lessonId={}", saved.getId());
        return getByIdWithAttendance(saved.getId());
    }

    public LessonDetailsResponse update(Long id, LessonUpdateRequest req) {
        log.info("Обновление занятия lessonId={}: teacherId={}, subjectId={}, groupId={}, date={}, pairNumber={}",
                id, req.getTeacherId(), req.getSubjectId(), req.getGroupId(), req.getDate(), req.getPairNumber());

        Lesson lesson = lessonRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Занятие с id=" + id + " не найдено"));

        applyUpdate(lesson, req.getTeacherId(), req.getSubjectId(), req.getGroupId(), req.getDate(), req.getPairNumber());
        try {
            lessonRepository.saveAndFlush(lesson);
        } catch (DataIntegrityViolationException ex) {
            throw new ConflictException("Занятие в этот слот уже существует (преподаватель/группа/дата/пара)");
        }

        attendanceRepository.deleteAllByLessonId(id);
        log.info("Занятие обновлено, attendance очищен: lessonId={}", id);

        return getByIdWithAttendance(id);
    }

    public void delete(Long id) {
        log.info("Удаление занятия lessonId={}", id);

        Lesson lesson = lessonRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Занятие с id=" + id + " не найдено"));

        attendanceRepository.deleteAllByLessonId(id);
        lessonRepository.delete(lesson);

        log.info("Занятие удалено: lessonId={}", id);
    }

    public LessonDetailsResponse updateAttendance(Long lessonId, LessonAttendanceUpdateRequest req) {
        int presentCount = req.getPresentStudentIds() == null ? 0 : req.getPresentStudentIds().size();
        log.info("Обновление посещаемости: lessonId={}, presentStudentIdsCount={}", lessonId, presentCount);

        Lesson lesson = lessonRepository.findById(lessonId)
                .orElseThrow(() -> new NotFoundException("Занятие с id=" + lessonId + " не найдено"));

        Long groupId = lesson.getGroup().getId();
        List<Student> groupStudents = studentRepository.findAllByGroupIdOrderByFullNameAsc(groupId);
        Set<Long> groupStudentIds = groupStudents.stream().map(Student::getId).collect(Collectors.toSet());

        List<Long> presentIds = Optional.ofNullable(req.getPresentStudentIds()).orElse(List.of());
        Set<Long> presentSet = new HashSet<>(presentIds);

        List<Long> invalid = presentSet.stream().filter(id -> !groupStudentIds.contains(id)).sorted().toList();
        if (!invalid.isEmpty()) {
            throw new BadRequestException("Есть студенты, не принадлежащие группе занятия", invalid.stream()
                    .map(x -> "studentId=" + x)
                    .toList());
        }

        attendanceRepository.deleteAllByLessonId(lessonId);

        List<Attendance> toSave = new ArrayList<>();
        for (Student s : groupStudents) {
            boolean present = presentSet.contains(s.getId());
            if (present) {
                toSave.add(new Attendance(null, lesson, s, true));
            }
        }
        attendanceRepository.saveAll(toSave);
        log.info("Посещаемость обновлена: lessonId={}, presentSaved={}", lessonId, toSave.size());

        return getByIdWithAttendance(lessonId);
    }

    private void applyUpdate(Lesson lesson, Long teacherId, Long subjectId, Long groupId, LocalDate date, Integer pairNumber) {
        Teacher teacher = teacherRepository.findById(teacherId)
                .orElseThrow(() -> new NotFoundException("Преподаватель с id=" + teacherId + " не найден"));
        Subject subject = subjectRepository.findById(subjectId)
                .orElseThrow(() -> new NotFoundException("Дисциплина с id=" + subjectId + " не найдена"));
        StudentGroup group = groupRepository.findById(groupId)
                .orElseThrow(() -> new NotFoundException("Группа с id=" + groupId + " не найдена"));

        lesson.setTeacher(teacher);
        lesson.setSubject(subject);
        lesson.setGroup(group);
        lesson.setLessonDate(date);
        lesson.setPairNumber(pairNumber);
    }

    private LessonListItemResponse toListItem(Lesson lesson) {
        return new LessonListItemResponse(
                lesson.getId(),
                lesson.getTeacher().getId(),
                lesson.getTeacher().getFullName(),
                lesson.getSubject().getId(),
                lesson.getSubject().getName(),
                lesson.getGroup().getId(),
                lesson.getGroup().getName(),
                lesson.getLessonDate(),
                lesson.getPairNumber()
        );
    }
}
