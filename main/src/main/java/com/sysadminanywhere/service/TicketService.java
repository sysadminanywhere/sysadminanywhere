package com.sysadminanywhere.service;

import com.sysadminanywhere.client.servicedesk.TicketServiceClient;
import com.sysadminanywhere.common.PageResponse;
import com.sysadminanywhere.common.incident.model.Category;
import com.sysadminanywhere.common.incident.model.CommentItem;
import com.sysadminanywhere.common.incident.model.Priority;
import com.sysadminanywhere.common.incident.model.TicketItem;
import com.sysadminanywhere.common.incident.model.TicketStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class TicketService {

    private final TicketServiceClient ticketServiceClient;

    public TicketService(TicketServiceClient ticketServiceClient) {
        this.ticketServiceClient = ticketServiceClient;
    }

    public Boolean ping() {
        try {
            ticketServiceClient.getTickets(0, 1, null, null, null, null);
            return true;
        } catch (Exception ex) {
            return false;
        }
    }

    public Page<TicketItem> getTickets(Pageable pageable, Map<String, Object> filters) {
        String status = filters != null ? (String) filters.get("status") : null;
        String priority = filters != null ? (String) filters.get("priority") : null;
        String category = filters != null ? (String) filters.get("category") : null;
        String assignee = filters != null ? (String) filters.get("assignee") : null;

        PageResponse<TicketItem> response = ticketServiceClient.getTickets(
                pageable.getPageNumber(),
                pageable.getPageSize(),
                status,
                priority,
                category,
                assignee
        );
        return new PageImpl<>(response.content(), PageRequest.of(response.page(), response.size()), response.totalElements());
    }

    public TicketItem getTicketById(Long id) {
        return ticketServiceClient.getTicket(id);
    }

    public TicketItem getTicketByNumber(String ticketNumber) {
        return ticketServiceClient.getTicketByNumber(ticketNumber);
    }

    public TicketItem createTicket(TicketItem ticket) {
        return ticketServiceClient.createTicket(ticket);
    }

    public TicketItem createTicketFromIncident(Long incidentId, String title, String description, String requester) {
        Map<String, Object> request = Map.of(
                "incidentId", incidentId,
                "title", title,
                "description", description,
                "requester", requester
        );
        return ticketServiceClient.createTicketFromIncident(request);
    }

    public TicketItem updateTicket(Long id, TicketItem ticket) {
        return ticketServiceClient.updateTicket(id, ticket);
    }

    public void deleteTicket(Long id) {
        ticketServiceClient.deleteTicket(id);
    }

    public TicketItem assignTicket(Long id, String assignee) {
        Map<String, String> request = Map.of("assignee", assignee);
        return ticketServiceClient.assignTicket(id, request);
    }

    public TicketItem resolveTicket(Long id, String resolution) {
        Map<String, String> request = Map.of("resolution", resolution);
        return ticketServiceClient.resolveTicket(id, request);
    }

    public TicketItem closeTicket(Long id) {
        return ticketServiceClient.closeTicket(id);
    }

    public List<CommentItem> getComments(Long ticketId) {
        return ticketServiceClient.getComments(ticketId);
    }

    public List<CommentItem> getPublicComments(Long ticketId) {
        return ticketServiceClient.getPublicComments(ticketId);
    }

    public CommentItem addComment(Long ticketId, CommentItem comment) {
        return ticketServiceClient.addComment(ticketId, comment);
    }

    public void deleteComment(Long commentId) {
        ticketServiceClient.deleteComment(commentId);
    }

}
