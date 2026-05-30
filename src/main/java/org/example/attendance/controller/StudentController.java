package org.example.attendance.controller;

import jakarta.validation.Valid;
import org.example.attendance.dto.ApiResponse;
import org.example.attendance.dto.student.StudentCreateRequest;
import org.example.attendance.dto.student.StudentResponse;
import org.example.attendance.dto.student.StudentUpdateRequest;
import org.example.attendance.service.StudentService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/students")
public class StudentController {
    private final StudentService service;

    public StudentController(StudentService service) {
        this.service = service;
    }

    @GetMapping("/{id}")
    public ApiResponse<StudentResponse> getById(@PathVariable Long id) {
        return ApiResponse.success(service.getById(id));
    }

    @GetMapping
    public ApiResponse<List<StudentResponse>> getByGroup(@RequestParam("groupId") Long groupId) {
        return ApiResponse.success(service.getByGroup(groupId));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<StudentResponse> create(@Valid @RequestBody StudentCreateRequest req) {
        return ApiResponse.success(service.create(req));
    }

    @PutMapping("/{id}")
    public ApiResponse<StudentResponse> update(@PathVariable Long id, @Valid @RequestBody StudentUpdateRequest req) {
        return ApiResponse.success(service.update(id, req));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        service.delete(id);
    }
}
