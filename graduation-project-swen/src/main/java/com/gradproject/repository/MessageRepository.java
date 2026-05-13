package com.gradproject.repository;

import com.gradproject.entity.Message;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface MessageRepository extends JpaRepository<Message, Long> {
    List<Message> findByGroupIdOrderBySentAtAsc(Long groupId);
    List<Message> findByGroupIdAndIdGreaterThanOrderBySentAtAsc(Long groupId, Long afterId);
}
