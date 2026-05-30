package org.example.attendance.controller;

import jakarta.validation.Valid;
import org.example.attendance.dto.ApiResponse;
import org.example.attendance.dto.subject.SubjectCreateRequest;
import org.example.attendance.dto.subject.SubjectResponse;
import org.example.attendance.dto.subject.SubjectUpdateRequest;
import org.example.attendance.service.SubjectService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/subjects")
public class SubjectController {
    private final SubjectService service;

    public SubjectController(SubjectService service) {
        this.service = service;
    }

    @GetMapping
    public ApiResponse<List<SubjectResponse>> getAll() {
        return ApiResponse.success(service.getAll());
    }

    @GetMapping("/{id}")
    public ApiResponse<SubjectResponse> getById(@PathVariable Long id) {
        return ApiResponse.success(service.getById(id));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<SubjectResponse> create(@Valid @RequestBody SubjectCreateRequest req) {
        return ApiResponse.success(service.create(req));
    }

    @PutMapping("/{id}")
    public ApiResponse<SubjectResponse> update(@PathVariable Long id, @Valid @RequestBody SubjectUpdateRequest req) {
        return ApiResponse.success(service.update(id, req));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        service.delete(id);
    }
}
