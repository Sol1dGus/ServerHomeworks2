package org.example.attendance.dto.lesson;

import org.example.attendance.dto.student.StudentResponse;

import java.time.LocalDate;
import java.util.List;

public class LessonDetailsResponse {
    private Long id;

    private Long teacherId;
    private String teacherFullName;

    private Long subjectId;
    private String subjectName;

    private Long groupId;
    private String groupName;

    private LocalDate date;
    private Integer pairNumber;

    private List<AttendanceItem> attendance;

    public LessonDetailsResponse() {
    }

    public LessonDetailsResponse(Long id, Long teacherId, String teacherFullName, Long subjectId, String subjectName,
                                 Long groupId, String groupName, LocalDate date, Integer pairNumber,
                                 List<AttendanceItem> attendance) {
        this.id = id;
        this.teacherId = teacherId;
        this.teacherFullName = teacherFullName;
        this.subjectId = subjectId;
        this.subjectName = subjectName;
        this.groupId = groupId;
        this.groupName = groupName;
        this.date = date;
        this.pairNumber = pairNumber;
        this.attendance = attendance;
    }

    public record AttendanceItem(Long studentId, String studentFullName, boolean present) {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getTeacherId() {
        return teacherId;
    }

    public void setTeacherId(Long teacherId) {
        this.teacherId = teacherId;
    }

    public String getTeacherFullName() {
        return teacherFullName;
    }

    public void setTeacherFullName(String teacherFullName) {
        this.teacherFullName = teacherFullName;
    }

    public Long getSubjectId() {
        return subjectId;
    }

    public void setSubjectId(Long subjectId) {
        this.subjectId = subjectId;
    }

    public String getSubjectName() {
        return subjectName;
    }

    public void setSubjectName(String subjectName) {
        this.subjectName = subjectName;
    }

    public Long getGroupId() {
        return groupId;
    }

    public void setGroupId(Long groupId) {
        this.groupId = groupId;
    }

    public String getGroupName() {
        return groupName;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
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

    public List<AttendanceItem> getAttendance() {
        return attendance;
    }

    public void setAttendance(List<AttendanceItem> attendance) {
        this.attendance = attendance;
    }
}

