package com.sysadminanywhere.incident.service;

import com.sysadminanywhere.common.incident.model.Category;
import com.sysadminanywhere.common.incident.model.CommentItem;
import com.sysadminanywhere.common.incident.model.Priority;
import com.sysadminanywhere.common.incident.model.TicketItem;
import com.sysadminanywhere.common.incident.model.TicketStatus;
import com.sysadminanywhere.incident.entity.CommentEntity;
import com.sysadminanywhere.incident.entity.TicketEntity;
import com.sysadminanywhere.incident.mapper.TicketMapper;
import com.sysadminanywhere.incident.repository.CommentRepository;
import com.sysadminanywhere.incident.repository.TicketRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional
public class TicketService {

    private final TicketRepository ticketRepository;
    private final CommentRepository commentRepository;
    private final TicketNumberGenerator ticketNumberGenerator;

    public TicketItem createTicket(TicketItem ticketItem) {
        TicketEntity entity = TicketMapper.toEntity(ticketItem);
        if (entity.getTicketNumber() == null) {
            entity.setTicketNumber(ticketNumberGenerator.generate());
        }
        entity = ticketRepository.save(entity);
        return TicketMapper.toItem(entity);
    }

    public TicketItem createTicketFromIncident(Long incidentId, String title, String description, String requester) {
        TicketEntity entity = TicketEntity.builder()
                .title(title)
                .description(description)
                .requester(requester)
                .incidentId(incidentId)
                .status(TicketStatus.OPEN)
                .priority(Priority.MEDIUM)
                .category(Category.OTHER)
                .ticketNumber(ticketNumberGenerator.generate())
                .build();
        entity = ticketRepository.save(entity);
        return TicketMapper.toItem(entity);
    }

    public Optional<TicketItem> getTicketById(Long id) {
        return ticketRepository.findById(id)
                .map(TicketMapper::toItem);
    }

    public Optional<TicketItem> getTicketByNumber(String ticketNumber) {
        return ticketRepository.findByTicketNumber(ticketNumber)
                .map(TicketMapper::toItem);
    }

    public TicketItem updateTicket(Long id, TicketItem ticketItem) {
        TicketEntity entity = ticketRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Ticket not found with id: " + id));
        
        TicketMapper.updateEntityFromItem(ticketItem, entity);
        entity = ticketRepository.save(entity);
        
        return TicketMapper.toItem(entity);
    }

    public void deleteTicket(Long id) {
        ticketRepository.deleteById(id);
    }

    public Page<TicketItem> getTickets(Pageable pageable) {
        return ticketRepository.findAll(pageable)
                .map(TicketMapper::toItem);
    }

    public Page<TicketItem> getTicketsWithFilters(
            TicketStatus status,
            Priority priority,
            Category category,
            String assignee,
            Pageable pageable) {
        return ticketRepository.findWithFilters(status, priority, category, assignee, pageable)
                .map(TicketMapper::toItem);
    }

    public Page<TicketItem> getTicketsByStatus(TicketStatus status, Pageable pageable) {
        return ticketRepository.findByStatus(status, pageable)
                .map(TicketMapper::toItem);
    }

    public List<TicketItem> getTicketsByAssignee(String assignee) {
        return TicketMapper.toItemList(ticketRepository.findByAssignee(assignee));
    }

    public List<TicketItem> getTicketsByIncidentId(Long incidentId) {
        return TicketMapper.toItemList(ticketRepository.findByIncidentId(incidentId));
    }

    public TicketItem assignTicket(Long id, String assignee) {
        TicketEntity entity = ticketRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Ticket not found with id: " + id));
        
        entity.setAssignee(assignee);
        if (entity.getStatus() == TicketStatus.OPEN) {
            entity.setStatus(TicketStatus.IN_PROGRESS);
        }
        entity = ticketRepository.save(entity);
        
        return TicketMapper.toItem(entity);
    }

    public TicketItem resolveTicket(Long id, String resolution, String resolvedBy) {
        TicketEntity entity = ticketRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Ticket not found with id: " + id));
        
        entity.setStatus(TicketStatus.RESOLVED);
        entity.setResolution(resolution);
        entity.setResolvedBy(resolvedBy);
        entity.setResolvedAt(LocalDateTime.now());
        entity = ticketRepository.save(entity);
        
        return TicketMapper.toItem(entity);
    }

    public TicketItem closeTicket(Long id, String closedBy) {
        TicketEntity entity = ticketRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Ticket not found with id: " + id));
        
        entity.setStatus(TicketStatus.CLOSED);
        entity.setResolvedBy(closedBy);
        entity.setResolvedAt(LocalDateTime.now());
        entity = ticketRepository.save(entity);
        
        return TicketMapper.toItem(entity);
    }

    // ============================================================
    // Comments
    // ============================================================

    public CommentItem addComment(Long ticketId, CommentItem commentItem) {
        commentItem.setTicketId(ticketId);
        CommentEntity entity = TicketMapper.toCommentEntity(commentItem);
        entity = commentRepository.save(entity);
        return TicketMapper.toCommentItem(entity);
    }

    public List<CommentItem> getComments(Long ticketId) {
        return TicketMapper.toCommentItemList(
                commentRepository.findByTicketIdOrderByCreatedAtAsc(ticketId)
        );
    }

    public List<CommentItem> getPublicComments(Long ticketId) {
        return TicketMapper.toCommentItemList(
                commentRepository.findByTicketIdAndIsInternalFalseOrderByCreatedAtAsc(ticketId)
        );
    }

    public void deleteComment(Long id) {
        commentRepository.deleteById(id);
    }

}
