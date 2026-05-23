package org.example.attendance.dto.lesson;

import jakarta.validation.constraints.NotNull;

import java.util.List;

public class LessonAttendanceUpdateRequest {
    @NotNull(message = "presentStudentIds обязателен")
    private List<Long> presentStudentIds;

    public LessonAttendanceUpdateRequest() {
    }

    public LessonAttendanceUpdateRequest(List<Long> presentStudentIds) {
        this.presentStudentIds = presentStudentIds;
    }

    public List<Long> getPresentStudentIds() {
        return presentStudentIds;
    }

    public void setPresentStudentIds(List<Long> presentStudentIds) {
        this.presentStudentIds = presentStudentIds;
    }
}

