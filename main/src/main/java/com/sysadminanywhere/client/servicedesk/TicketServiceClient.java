package com.sysadminanywhere.client.servicedesk;

import com.sysadminanywhere.common.PageResponse;
import com.sysadminanywhere.common.incident.model.CommentItem;
import com.sysadminanywhere.common.incident.model.TicketItem;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.service.annotation.DeleteExchange;
import org.springframework.web.service.annotation.GetExchange;
import org.springframework.web.service.annotation.PostExchange;
import org.springframework.web.service.annotation.PutExchange;

import java.util.List;
import java.util.Map;

public interface TicketServiceClient {

    @GetExchange("/api/tickets")
    PageResponse<TicketItem> getTickets(
            @RequestParam int page,
            @RequestParam int size,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String priority,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String assignee
    );

    @GetExchange("/api/tickets/{id}")
    TicketItem getTicket(@PathVariable Long id);

    @GetExchange("/api/tickets/number/{ticketNumber}")
    TicketItem getTicketByNumber(@PathVariable String ticketNumber);

    @PostExchange("/api/tickets")
    TicketItem createTicket(@RequestBody TicketItem ticket);

    @PostExchange("/api/tickets/from-incident")
    TicketItem createTicketFromIncident(@RequestBody Map<String, Object> request);

    @PutExchange("/api/tickets/{id}")
    TicketItem updateTicket(@PathVariable Long id, @RequestBody TicketItem ticket);

    @DeleteExchange("/api/tickets/{id}")
    void deleteTicket(@PathVariable Long id);

    @PostExchange("/api/tickets/{id}/assign")
    TicketItem assignTicket(@PathVariable Long id, @RequestBody Map<String, String> request);

    @PostExchange("/api/tickets/{id}/resolve")
    TicketItem resolveTicket(@PathVariable Long id, @RequestBody Map<String, String> request);

    @PostExchange("/api/tickets/{id}/close")
    TicketItem closeTicket(@PathVariable Long id);

    @GetExchange("/api/tickets/{id}/comments")
    List<CommentItem> getComments(@PathVariable Long id);

    @GetExchange("/api/tickets/{id}/comments/public")
    List<CommentItem> getPublicComments(@PathVariable Long id);

    @PostExchange("/api/tickets/{id}/comments")
    CommentItem addComment(@PathVariable Long id, @RequestBody CommentItem comment);

    @DeleteExchange("/api/tickets/comments/{commentId}")
    void deleteComment(@PathVariable Long commentId);

}
