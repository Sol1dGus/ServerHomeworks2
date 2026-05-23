package org.example.attendance.dto.group;

public class StudentGroupResponse {
    private final Long id;
    private final String name;

    public StudentGroupResponse(Long id, String name) {
        this.id = id;
        this.name = name;
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }
}
