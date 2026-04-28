package com.sysadminanywhere.incident.repository;

import com.sysadminanywhere.incident.entity.CommentEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CommentRepository extends JpaRepository<CommentEntity, Long> {

    List<CommentEntity> findByTicketIdOrderByCreatedAtAsc(Long ticketId);

    List<CommentEntity> findByTicketIdAndIsInternalFalseOrderByCreatedAtAsc(Long ticketId);

}
