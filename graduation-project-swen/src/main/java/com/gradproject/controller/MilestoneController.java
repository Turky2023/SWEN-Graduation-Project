package com.gradproject.controller;

import com.gradproject.dto.MilestoneDTO;
import com.gradproject.entity.Milestone;
import com.gradproject.service.MilestoneService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/milestones")
public class MilestoneController {

    private final MilestoneService milestoneService;

    public MilestoneController(MilestoneService milestoneService) {
        this.milestoneService = milestoneService;
    }

    @PostMapping
    public ResponseEntity<?> create(@RequestBody MilestoneDTO dto) {
        try {
            Milestone m = milestoneService.createMilestone(dto);
            return ResponseEntity.ok(mapMilestone(m));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> update(@PathVariable Long id, @RequestBody MilestoneDTO dto) {
        try {
            Milestone m = milestoneService.updateMilestone(id, dto);
            return ResponseEntity.ok(mapMilestone(m));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/group/{groupId}")
    public ResponseEntity<?> getByGroup(@PathVariable Long groupId) {
        List<Milestone> milestones = milestoneService.findByGroupId(groupId);
        return ResponseEntity.ok(milestones.stream().map(this::mapMilestone).toList());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable Long id) {
        try {
            milestoneService.deleteMilestone(id);
            return ResponseEntity.ok(Map.of("message", "Milestone deleted"));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/{id}/template")
    public ResponseEntity<?> uploadTemplate(@PathVariable Long id, @RequestParam("file") MultipartFile file) {
        try {
            milestoneService.uploadTemplate(id, file);
            return ResponseEntity.ok(Map.of("message", "Template uploaded"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/{id}/template")
    public ResponseEntity<?> downloadTemplate(@PathVariable Long id) {
        Milestone m = milestoneService.findById(id);
        if (m.getTemplateFileData() == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(m.getTemplateFileType()))
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + m.getTemplateFileName() + "\"")
                .body(m.getTemplateFileData());
    }

    private Map<String, Object> mapMilestone(Milestone m) {
        Map<String, Object> map = new HashMap<>();
        map.put("id", m.getId());
        map.put("title", m.getTitle());
        map.put("deadline", m.getDeadline().toString());
        map.put("type", m.getType().name());
        map.put("groupId", m.getGroup().getId());
        map.put("hasTemplate", m.getTemplateFileData() != null);
        map.put("templateFileName", m.getTemplateFileName());
        return map;
    }
}
