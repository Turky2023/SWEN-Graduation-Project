package com.gradproject.controller;

import com.gradproject.dto.GroupDTO;
import com.gradproject.entity.GroupMember;
import com.gradproject.entity.ProjectGroup;
import com.gradproject.entity.Role;
import com.gradproject.entity.User;
import com.gradproject.service.GroupService;
import com.gradproject.service.SubmissionService;
import com.gradproject.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/groups")
public class GroupController {

    private final GroupService groupService;
    private final UserService userService;
    private final SubmissionService submissionService;

    public GroupController(GroupService groupService, UserService userService, SubmissionService submissionService) {
        this.groupService = groupService;
        this.userService = userService;
        this.submissionService = submissionService;
    }

    @PostMapping
    public ResponseEntity<?> createGroup(@RequestBody GroupDTO dto, Authentication auth) {
        try {
            User user = userService.findByEmail(auth.getName()).orElseThrow();
            if (user.getRole() != Role.SUPERVISOR) {
                return ResponseEntity.status(403).body(Map.of("error", "Only supervisors can create groups"));
            }
            ProjectGroup group = groupService.createGroup(dto, user);
            return ResponseEntity.ok(mapGroup(group));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping
    public ResponseEntity<?> getMyGroups(Authentication auth) {
        User user = userService.findByEmail(auth.getName()).orElseThrow();
        List<ProjectGroup> groups;
        if (user.getRole().name().equals("SUPERVISOR")) {
            groups = groupService.findBySupervisor(user.getId());
        } else {
            groups = groupService.findByStudent(user.getId());
        }
        return ResponseEntity.ok(groups.stream().map(this::mapGroup).toList());
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getGroup(@PathVariable Long id) {
        try {
            ProjectGroup group = groupService.findById(id);
            Map<String, Object> result = mapGroup(group);
            result.put("progress", submissionService.calculateProgress(id));
            return ResponseEntity.ok(result);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/{id}/members")
    public ResponseEntity<?> addMember(@PathVariable Long id, @RequestBody Map<String, Long> body, Authentication auth) {
        try {
            User user = userService.findByEmail(auth.getName()).orElseThrow();
            if (user.getRole() != Role.SUPERVISOR) {
                return ResponseEntity.status(403).body(Map.of("error", "Only supervisors can add members"));
            }
            groupService.addMember(id, body.get("studentId"));
            return ResponseEntity.ok(Map.of("message", "Member added"));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @DeleteMapping("/{groupId}/members/{memberId}")
    public ResponseEntity<?> removeMember(@PathVariable Long groupId, @PathVariable Long memberId, Authentication auth) {
        try {
            User user = userService.findByEmail(auth.getName()).orElseThrow();
            if (user.getRole() != Role.SUPERVISOR) {
                return ResponseEntity.status(403).body(Map.of("error", "Only supervisors can remove members"));
            }
            groupService.removeMember(groupId, memberId);
            return ResponseEntity.ok(Map.of("message", "Member removed"));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/{id}/members")
    public ResponseEntity<?> getMembers(@PathVariable Long id) {
        List<GroupMember> members = groupService.getMembers(id);
        return ResponseEntity.ok(members.stream().map(m -> {
            Map<String, Object> map = new HashMap<>();
            map.put("id", m.getId());
            map.put("studentId", m.getStudent().getId());
            map.put("studentName", m.getStudent().getName());
            map.put("studentEmail", m.getStudent().getEmail());
            map.put("studentIdNumber", m.getStudent().getStudentId());
            return map;
        }).toList());
    }

    private Map<String, Object> mapGroup(ProjectGroup g) {
        Map<String, Object> m = new HashMap<>();
        m.put("id", g.getId());
        m.put("name", g.getName());
        if (g.getSupervisor() != null) {
            m.put("supervisorId", g.getSupervisor().getId());
            m.put("supervisorName", g.getSupervisor().getName());
        }
        return m;
    }
}
