package org.example.attendance.dto.teacher;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class TeacherUpdateRequest {
    @NotBlank(message = "ФИО обязательно")
    @Size(max = 200, message = "ФИО не должно превышать 200 символов")
    private String fullName;

    public TeacherUpdateRequest() {
    }

    public TeacherUpdateRequest(String fullName) {
        this.fullName = fullName;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }
}

