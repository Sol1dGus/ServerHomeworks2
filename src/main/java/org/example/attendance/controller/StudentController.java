package org.example.attendance.controller;

import jakarta.validation.Valid;
import org.example.attendance.dto.ApiResponse;
import org.example.attendance.dto.student.StudentCreateRequest;
import org.example.attendance.dto.student.StudentResponse;
import org.example.attendance.dto.student.StudentUpdateRequest;
import org.example.attendance.exception.ForbiddenException;
import org.example.attendance.security.SecurityUtils;
import org.example.attendance.service.StudentService;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/students")
@Tag(name = "Students", description = "Управление студентами")
public class StudentController {
    private final StudentService service;

    public StudentController(StudentService service) {
        this.service = service;
    }

    @GetMapping("/{id}")
    public ApiResponse<StudentResponse> getById(@PathVariable Long id) {
        StudentResponse resp = service.getById(id);
        if (SecurityUtils.isStudent() && !resp.getGroupId().equals(SecurityUtils.getCurrentGroupId())) {
            throw new ForbiddenException("Доступ запрещён: можно просматривать только студентов своей группы");
        }
        return ApiResponse.success(resp);
    }

    @GetMapping
    public ApiResponse<List<StudentResponse>> getByGroup(
            @RequestParam(name = "groupId", required = false) Long groupId) {
        if (SecurityUtils.isStudent()) {
            groupId = SecurityUtils.getCurrentGroupId();
        }
        if (groupId == null) {
            throw new org.example.attendance.exception.BadRequestException("Параметр groupId обязателен");
        }
        return ApiResponse.success(service.getByGroup(groupId));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<StudentResponse> create(@Valid @RequestBody StudentCreateRequest req) {
        return ApiResponse.success(service.create(req));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<StudentResponse> update(@PathVariable Long id, @Valid @RequestBody StudentUpdateRequest req) {
        return ApiResponse.success(service.update(id, req));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasRole('ADMIN')")
    public void delete(@PathVariable Long id) {
        service.delete(id);
    }
}
