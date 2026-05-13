package com.gradproject.controller;

import com.gradproject.dto.FeedbackDTO;
import com.gradproject.entity.Feedback;
import com.gradproject.entity.Submission;
import com.gradproject.service.FeedbackService;
import com.gradproject.service.SubmissionService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/submissions")
public class SubmissionController {

    private final SubmissionService submissionService;
    private final FeedbackService feedbackService;

    public SubmissionController(SubmissionService submissionService, FeedbackService feedbackService) {
        this.submissionService = submissionService;
        this.feedbackService = feedbackService;
    }

    @PostMapping("/upload")
    public ResponseEntity<?> upload(@RequestParam("groupId") Long groupId,
                                    @RequestParam("milestoneId") Long milestoneId,
                                    @RequestParam("file") MultipartFile file) {
        try {
            Submission s = submissionService.upload(groupId, milestoneId, file);
            return ResponseEntity.ok(mapSubmission(s));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable Long id) {
        try {
            submissionService.delete(id);
            return ResponseEntity.ok(Map.of("message", "Submission deleted"));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/group/{groupId}")
    public ResponseEntity<?> getByGroup(@PathVariable Long groupId) {
        List<Submission> submissions = submissionService.findByGroupId(groupId);
        return ResponseEntity.ok(submissions.stream().map(this::mapSubmission).toList());
    }

    @GetMapping("/{id}/download")
    public ResponseEntity<?> download(@PathVariable Long id) {
        Submission s = submissionService.findById(id);
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(s.getFileType()))
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + s.getFileName() + "\"")
                .body(s.getFileData());
    }

    @PostMapping("/{id}/feedback")
    public ResponseEntity<?> leaveFeedback(@PathVariable Long id, @RequestBody FeedbackDTO dto) {
        try {
            Feedback f = feedbackService.leaveFeedback(id, dto);
            Map<String, Object> result = new HashMap<>();
            result.put("id", f.getId());
            result.put("comment", f.getComment());
            result.put("grade", f.getGrade());
            result.put("createdAt", f.getCreatedAt().toString());
            return ResponseEntity.ok(result);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/progress/{groupId}")
    public ResponseEntity<?> getProgress(@PathVariable Long groupId) {
        int progress = submissionService.calculateProgress(groupId);
        return ResponseEntity.ok(Map.of("progress", progress));
    }

    private Map<String, Object> mapSubmission(Submission s) {
        Map<String, Object> m = new HashMap<>();
        m.put("id", s.getId());
        m.put("fileName", s.getFileName());
        m.put("fileType", s.getFileType());
        m.put("status", s.getStatus().name());
        m.put("uploadedAt", s.getUploadedAt().toString());
        m.put("milestoneId", s.getMilestone().getId());
        m.put("milestoneTitle", s.getMilestone().getTitle());
        m.put("milestoneType", s.getMilestone().getType().name());
        m.put("groupId", s.getGroup().getId());
        if (s.getFeedback() != null) {
            Map<String, Object> fb = new HashMap<>();
            fb.put("id", s.getFeedback().getId());
            fb.put("comment", s.getFeedback().getComment());
            fb.put("grade", s.getFeedback().getGrade());
            fb.put("createdAt", s.getFeedback().getCreatedAt().toString());
            m.put("feedback", fb);
        }
        return m;
    }
}
