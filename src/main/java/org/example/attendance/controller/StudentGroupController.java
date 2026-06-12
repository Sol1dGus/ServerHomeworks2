package org.example.attendance.controller;

import jakarta.validation.Valid;
import org.example.attendance.dto.ApiResponse;
import org.example.attendance.dto.group.StudentGroupCreateRequest;
import org.example.attendance.dto.group.StudentGroupResponse;
import org.example.attendance.dto.group.StudentGroupUpdateRequest;
import org.example.attendance.service.StudentGroupService;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/groups")
@Tag(name = "Groups", description = "Управление группами студентов")
public class StudentGroupController {
    private final StudentGroupService service;

    public StudentGroupController(StudentGroupService service) {
        this.service = service;
    }

    @GetMapping
    public ApiResponse<List<StudentGroupResponse>> getAll() {
        return ApiResponse.success(service.getAll());
    }

    @GetMapping("/{id}")
    public ApiResponse<StudentGroupResponse> getById(@PathVariable Long id) {
        return ApiResponse.success(service.getById(id));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<StudentGroupResponse> create(@Valid @RequestBody StudentGroupCreateRequest req) {
        return ApiResponse.success(service.create(req));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<StudentGroupResponse> update(@PathVariable Long id, @Valid @RequestBody StudentGroupUpdateRequest req) {
        return ApiResponse.success(service.update(id, req));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasRole('ADMIN')")
    public void delete(@PathVariable Long id) {
        service.delete(id);
    }
}
