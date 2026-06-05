package org.example.attendance.controller;

import jakarta.validation.Valid;
import org.example.attendance.dto.ApiResponse;
import org.example.attendance.dto.lesson.*;
import org.example.attendance.exception.ForbiddenException;
import org.example.attendance.security.SecurityUtils;
import org.example.attendance.service.LessonService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/lessons")
public class LessonController {
    private final LessonService service;

    public LessonController(LessonService service) {
        this.service = service;
    }

    @GetMapping("/{id}")
    public ApiResponse<LessonDetailsResponse> getById(@PathVariable Long id) {
        LessonDetailsResponse resp = service.getByIdWithAttendance(id);
        if (SecurityUtils.isStudent() && !resp.getGroupId().equals(SecurityUtils.getCurrentGroupId())) {
            throw new ForbiddenException("Доступ запрещён: можно просматривать только занятия своей группы");
        }
        return ApiResponse.success(resp);
    }

    @GetMapping
    public ApiResponse<Page<LessonListItemResponse>> getList(
            @RequestParam("from") LocalDate from,
            @RequestParam("to") LocalDate to,
            @RequestParam(name = "groupId", required = false) Long groupId,
            @RequestParam(name = "teacherId", required = false) Long teacherId,
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "20") int size
    ) {
        if (SecurityUtils.isStudent()) {
            groupId = SecurityUtils.getCurrentGroupId();
        }
        Pageable pageable = PageRequest.of(page, size);
        return ApiResponse.success(service.getList(from, to, groupId, teacherId, pageable));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAnyRole('TEACHER', 'ADMIN')")
    public ApiResponse<LessonDetailsResponse> create(@Valid @RequestBody LessonCreateRequest req) {
        return ApiResponse.success(service.create(req));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('TEACHER', 'ADMIN')")
    public ApiResponse<LessonDetailsResponse> update(@PathVariable Long id, @Valid @RequestBody LessonUpdateRequest req) {
        return ApiResponse.success(service.update(id, req));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasAnyRole('TEACHER', 'ADMIN')")
    public void delete(@PathVariable Long id) {
        service.delete(id);
    }

    @PutMapping("/{id}/attendance")
    @PreAuthorize("hasAnyRole('TEACHER', 'ADMIN')")
    public ApiResponse<LessonDetailsResponse> updateAttendance(@PathVariable Long id,
                                                               @Valid @RequestBody LessonAttendanceUpdateRequest req) {
        return ApiResponse.success(service.updateAttendance(id, req));
    }
}
