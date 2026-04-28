package com.sysadminanywhere.incident.controller;

import com.sysadminanywhere.common.PageResponse;
import com.sysadminanywhere.common.incident.model.Category;
import com.sysadminanywhere.common.incident.model.CommentItem;
import com.sysadminanywhere.common.incident.model.Priority;
import com.sysadminanywhere.common.incident.model.TicketItem;
import com.sysadminanywhere.common.incident.model.TicketStatus;
import com.sysadminanywhere.incident.service.TicketService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Objects;

@Slf4j
@RestController
@RequestMapping("/api/tickets")
@RequiredArgsConstructor
public class TicketController {

    private final TicketService ticketService;

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public PageResponse<TicketItem> getTickets(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String priority,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String assignee
    ) {
        Pageable pageable = Pageable.ofSize(size).withPage(page);
        Page<TicketItem> result;

        TicketStatus statusFilter = parseStatus(status);
        Priority priorityFilter = parsePriority(priority);
        Category categoryFilter = parseCategory(category);

        result = ticketService.getTicketsWithFilters(
                statusFilter, priorityFilter, categoryFilter, assignee, pageable
        );

        return new PageResponse<>(
                result.getContent(),
                result.getNumber(),
                result.getSize(),
                result.getTotalElements(),
                result.getTotalPages()
        );
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<TicketItem> getTicket(@PathVariable Long id) {
        return ticketService.getTicketById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/number/{ticketNumber}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<TicketItem> getTicketByNumber(@PathVariable String ticketNumber) {
        return ticketService.getTicketByNumber(ticketNumber)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<TicketItem> createTicket(@Valid @RequestBody TicketItem request) {
        TicketItem created = ticketService.createTicket(request);
        log.info("Ticket created: {}", created.getId());
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PostMapping("/from-incident")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<TicketItem> createTicketFromIncident(@RequestBody Map<String, Object> request) {
        Long incidentId = ((Number) request.get("incidentId")).longValue();
        String title = (String) request.get("title");
        String description = (String) request.get("description");
        String requester = (String) request.getOrDefault("requester", "System");

        TicketItem created = ticketService.createTicketFromIncident(incidentId, title, description, requester);
        log.info("Ticket created from incident: {}", created.getId());
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<TicketItem> updateTicket(@PathVariable Long id, @Valid @RequestBody TicketItem request) {
        request.setId(id);
        TicketItem updated = ticketService.updateTicket(id, request);
        log.info("Ticket updated: {}", id);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteTicket(@PathVariable Long id) {
        ticketService.deleteTicket(id);
        log.info("Ticket deleted: {}", id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/assign")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<TicketItem> assignTicket(@PathVariable Long id, @RequestBody Map<String, String> request) {
        String assignee = request.get("assignee");
        TicketItem updated = ticketService.assignTicket(id, assignee);
        log.info("Ticket assigned: {} to {}", id, assignee);
        return ResponseEntity.ok(updated);
    }

    @PostMapping("/{id}/resolve")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<TicketItem> resolveTicket(@PathVariable Long id, @RequestBody Map<String, String> request) {
        String resolution = request.get("resolution");
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String resolvedBy = authentication.getName();
        
        TicketItem updated = ticketService.resolveTicket(id, resolution, resolvedBy);
        log.info("Ticket resolved: {}", id);
        return ResponseEntity.ok(updated);
    }

    @PostMapping("/{id}/close")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<TicketItem> closeTicket(@PathVariable Long id) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String closedBy = authentication.getName();
        
        TicketItem updated = ticketService.closeTicket(id, closedBy);
        log.info("Ticket closed: {}", id);
        return ResponseEntity.ok(updated);
    }

    @GetMapping("/{id}/comments")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<CommentItem>> getComments(@PathVariable Long id) {
        List<CommentItem> comments = ticketService.getComments(id);
        return ResponseEntity.ok(comments);
    }

    @GetMapping("/{id}/comments/public")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<CommentItem>> getPublicComments(@PathVariable Long id) {
        List<CommentItem> comments = ticketService.getPublicComments(id);
        return ResponseEntity.ok(comments);
    }

    @PostMapping("/{id}/comments")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<CommentItem> addComment(@PathVariable Long id, @Valid @RequestBody CommentItem request) {
        CommentItem created = ticketService.addComment(id, request);
        log.info("Comment added to ticket: {}", id);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @DeleteMapping("/comments/{commentId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteComment(@PathVariable Long commentId) {
        ticketService.deleteComment(commentId);
        log.info("Comment deleted: {}", commentId);
        return ResponseEntity.noContent().build();
    }

    private TicketStatus parseStatus(String status) {
        if (status == null || status.equalsIgnoreCase("ALL")) {
            return null;
        }
        try {
            return TicketStatus.valueOf(status.toUpperCase().replace(" ", "_"));
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    private Priority parsePriority(String priority) {
        if (priority == null || priority.equalsIgnoreCase("ALL")) {
            return null;
        }
        try {
            return Priority.valueOf(priority.toUpperCase());
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    private Category parseCategory(String category) {
        if (category == null || category.equalsIgnoreCase("ALL")) {
            return null;
        }
        try {
            return Category.valueOf(category.toUpperCase());
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

}
