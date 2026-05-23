package org.example.attendance.dto.student;

public class StudentResponse {
    private final Long id;
    private final String fullName;
    private final Long groupId;
    private final String groupName;

    public StudentResponse(Long id, String fullName, Long groupId, String groupName) {
        this.id = id;
        this.fullName = fullName;
        this.groupId = groupId;
        this.groupName = groupName;
    }

    public Long getId() {
        return id;
    }

    public String getFullName() {
        return fullName;
    }

    public Long getGroupId() {
        return groupId;
    }

    public String getGroupName() {
        return groupName;
    }
}
