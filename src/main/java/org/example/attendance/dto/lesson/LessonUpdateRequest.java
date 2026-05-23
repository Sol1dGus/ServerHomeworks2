package org.example.attendance.dto.lesson;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public class LessonUpdateRequest {
    @NotNull(message = "teacherId обязателен")
    private Long teacherId;

    @NotNull(message = "subjectId обязателен")
    private Long subjectId;

    @NotNull(message = "groupId обязателен")
    private Long groupId;

    @NotNull(message = "Дата занятия обязательна")
    private LocalDate date;

    @NotNull(message = "Номер пары обязателен")
    @Min(value = 1, message = "Номер пары должен быть от 1")
    @Max(value = 10, message = "Номер пары должен быть до 10")
    private Integer pairNumber;

    public LessonUpdateRequest() {
    }

    public Long getTeacherId() {
        return teacherId;
    }

    public void setTeacherId(Long teacherId) {
        this.teacherId = teacherId;
    }

    public Long getSubjectId() {
        return subjectId;
    }

    public void setSubjectId(Long subjectId) {
        this.subjectId = subjectId;
    }

    public Long getGroupId() {
        return groupId;
    }

    public void setGroupId(Long groupId) {
        this.groupId = groupId;
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public Integer getPairNumber() {
        return pairNumber;
    }

    public void setPairNumber(Integer pairNumber) {
        this.pairNumber = pairNumber;
    }
}

