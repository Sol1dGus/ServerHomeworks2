package org.example.attendance.dto.subject;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class SubjectUpdateRequest {
    @NotBlank(message = "Название обязательно")
    @Size(max = 150, message = "Название не должно превышать 150 символов")
    private String name;

    public SubjectUpdateRequest() {
    }

    public SubjectUpdateRequest(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}

