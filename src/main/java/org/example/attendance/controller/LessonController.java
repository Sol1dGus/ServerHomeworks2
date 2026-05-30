package org.example.attendance.controller;

import jakarta.validation.Valid;
import org.example.attendance.dto.ApiResponse;
import org.example.attendance.dto.lesson.*;
import org.example.attendance.service.LessonService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
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
        return ApiResponse.success(service.getByIdWithAttendance(id));
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
        Pageable pageable = PageRequest.of(page, size);
        return ApiResponse.success(service.getList(from, to, groupId, teacherId, pageable));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<LessonDetailsResponse> create(@Valid @RequestBody LessonCreateRequest req) {
        return ApiResponse.success(service.create(req));
    }

    @PutMapping("/{id}")
    public ApiResponse<LessonDetailsResponse> update(@PathVariable Long id, @Valid @RequestBody LessonUpdateRequest req) {
        return ApiResponse.success(service.update(id, req));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        service.delete(id);
    }

    @PutMapping("/{id}/attendance")
    public ApiResponse<LessonDetailsResponse> updateAttendance(@PathVariable Long id,
                                                               @Valid @RequestBody LessonAttendanceUpdateRequest req) {
        return ApiResponse.success(service.updateAttendance(id, req));
    }
}
