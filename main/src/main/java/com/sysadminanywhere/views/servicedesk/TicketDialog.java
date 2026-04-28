package com.sysadminanywhere.views.servicedesk;

import com.sysadminanywhere.common.incident.model.Category;
import com.sysadminanywhere.common.incident.model.CommentItem;
import com.sysadminanywhere.common.incident.model.Priority;
import com.sysadminanywhere.common.incident.model.TicketItem;
import com.sysadminanywhere.common.incident.model.TicketStatus;
import com.sysadminanywhere.service.LocaleService;
import com.sysadminanywhere.service.TicketService;
import com.sysadminanywhere.service.Utils;
import org.springframework.context.MessageSource;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.tabs.Tab;
import com.vaadin.flow.component.tabs.TabSheet;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.textfield.TextField;

import java.util.List;

public class TicketDialog extends Dialog {

    private final TicketService ticketService;
    private final TicketItem ticket;
    private final MessageSource messageSource;
    private final LocaleService localeService;
    private final Runnable onSearch;

    private TextField txtTitle;
    private TextArea txtDescription;
    private TextField txtRequester;
    private ComboBox<String> comboStatus;
    private ComboBox<String> comboPriority;
    private ComboBox<String> comboCategory;
    private TextField txtAssignee;
    private TextArea txtResolution;

    public TicketDialog(TicketService ticketService, TicketItem ticket, MessageSource messageSource, LocaleService localeService, Runnable onSearch) {
        this.ticketService = ticketService;
        this.ticket = ticket;
        this.messageSource = messageSource;
        this.localeService = localeService;
        this.onSearch = onSearch;

        setHeaderTitle(ticket.getId() == null ? getMessage("ticket_dialog.create_title") : getMessage("ticket_dialog.title"));
        setWidth("800px");
        setHeight("600px");

        TabSheet tabSheet = new TabSheet();
        tabSheet.setSizeFull();

        Tab detailsTab = tabSheet.add(getMessage("ticket_dialog.title"), createDetailsLayout());
        
        if (ticket.getId() != null) {
            Tab commentsTab = tabSheet.add(getMessage("ticket_dialog.comments"), createCommentsLayout());
        }

        add(tabSheet);

        Button saveButton = new Button(getMessage("common.save"), e -> {
            try {
                if (ticket.getId() == null) {
                    TicketItem newTicket = TicketItem.builder()
                            .title(txtTitle.getValue())
                            .description(txtDescription.getValue())
                            .status(TicketStatus.valueOf(comboStatus.getValue().toUpperCase().replace(" ", "_")))
                            .priority(Priority.valueOf(comboPriority.getValue().toUpperCase()))
                            .category(Category.valueOf(comboCategory.getValue().toUpperCase()))
                            .requester(txtRequester.getValue())
                            .assignee(txtAssignee.getValue())
                            .build();
                    
                    ticketService.createTicket(newTicket);
                    
                    onSearch.run();
                    
                    Notification notification = Notification.show(getMessage("ticket_dialog.ticket_created"));
                    notification.addThemeVariants(NotificationVariant.LUMO_SUCCESS);
                } else {
                    ticket.setStatus(TicketStatus.valueOf(comboStatus.getValue().toUpperCase().replace(" ", "_")));
                    ticket.setPriority(Priority.valueOf(comboPriority.getValue().toUpperCase()));
                    ticket.setCategory(Category.valueOf(comboCategory.getValue().toUpperCase()));
                    ticket.setAssignee(txtAssignee.getValue());
                    
                    if (ticket.getStatus() == TicketStatus.RESOLVED && !txtResolution.getValue().isBlank()) {
                        ticket.setResolution(txtResolution.getValue());
                        ticketService.resolveTicket(ticket.getId(), txtResolution.getValue());
                    } else {
                        ticketService.updateTicket(ticket.getId(), ticket);
                    }
                    
                    onSearch.run();
                    
                    Notification notification = Notification.show(getMessage("ticket_dialog.ticket_updated"));
                    notification.addThemeVariants(NotificationVariant.LUMO_SUCCESS);
                }
            } catch (Exception ex) {
                Notification notification = Notification.show(ex.getMessage());
                notification.addThemeVariants(NotificationVariant.LUMO_ERROR);
            }

            close();
        });

        saveButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        Button cancelButton = new Button(getMessage("common.cancel"), e -> close());
        getFooter().add(cancelButton);
        getFooter().add(saveButton);
    }

    private String getMessage(String key) {
        return messageSource.getMessage(key, null, localeService.getCurrentLocale());
    }

    private VerticalLayout createDetailsLayout() {
        VerticalLayout layout = new VerticalLayout();
        layout.setPadding(false);
        layout.setSpacing(false);

        FormLayout formLayout = new FormLayout();

        TextField txtTicketNumber = new TextField(getMessage("ticket_dialog.ticket_number"));
        txtTicketNumber.setValue(ticket.getTicketNumber() != null ? ticket.getTicketNumber() : "");
        txtTicketNumber.setReadOnly(true);
        formLayout.setColspan(txtTicketNumber, 2);

        txtTitle = new TextField(getMessage("ticket_dialog.title"));
        txtTitle.setValue(ticket.getTitle() != null ? ticket.getTitle() : "");
        txtTitle.setReadOnly(ticket.getId() != null);
        formLayout.setColspan(txtTitle, 2);

        txtDescription = new TextArea(getMessage("ticket_dialog.description"));
        txtDescription.setValue(ticket.getDescription() != null ? ticket.getDescription() : "");
        txtDescription.setReadOnly(ticket.getId() != null);
        txtDescription.setMinHeight("100px");
        formLayout.setColspan(txtDescription, 2);

        comboStatus = new ComboBox<>(getMessage("ticket_dialog.status"));
        comboStatus.setItems("Open", "In Progress", "Resolved", "On Hold", "Closed");
        comboStatus.setValue(ticket.getStatus() != null ? ticket.getStatus().name().replace("_", " ") : "Open");

        comboPriority = new ComboBox<>(getMessage("ticket_dialog.priority"));
        comboPriority.setItems("Low", "Medium", "High", "Critical");
        comboPriority.setValue(ticket.getPriority() != null ? ticket.getPriority().name() : "Medium");

        comboCategory = new ComboBox<>(getMessage("ticket_dialog.category"));
        comboCategory.setItems("Hardware", "Software", "Network", "Access", "Other");
        comboCategory.setValue(ticket.getCategory() != null ? ticket.getCategory().name() : "Other");

        txtRequester = new TextField(getMessage("ticket_dialog.requester"));
        txtRequester.setValue(ticket.getRequester() != null ? ticket.getRequester() : "");
        txtRequester.setReadOnly(ticket.getId() != null);

        txtAssignee = new TextField(getMessage("ticket_dialog.assignee"));
        txtAssignee.setValue(ticket.getAssignee() != null ? ticket.getAssignee() : "");

        TextField txtCreatedAt = new TextField(getMessage("ticket_dialog.created_at"));
        txtCreatedAt.setValue(ticket.getCreatedAt() != null ? Utils.formatLocalDateTime(ticket.getCreatedAt()) : "");
        txtCreatedAt.setReadOnly(true);

        TextField txtResolvedAt = new TextField(getMessage("ticket_dialog.resolved_at"));
        txtResolvedAt.setValue(ticket.getResolvedAt() != null ? Utils.formatLocalDateTime(ticket.getResolvedAt()) : "");
        txtResolvedAt.setReadOnly(true);

        TextField txtResolvedBy = new TextField(getMessage("ticket_dialog.resolved_by"));
        txtResolvedBy.setValue(ticket.getResolvedBy() != null ? ticket.getResolvedBy() : "");
        txtResolvedBy.setReadOnly(true);

        txtResolution = new TextArea(getMessage("ticket_dialog.resolution"));
        txtResolution.setValue(ticket.getResolution() != null ? ticket.getResolution() : "");
        txtResolution.setReadOnly(ticket.getStatus() == TicketStatus.CLOSED || ticket.getStatus() == TicketStatus.RESOLVED);
        txtResolution.setMinHeight("80px");
        formLayout.setColspan(txtResolution, 2);

        formLayout.add(txtTicketNumber, txtTitle, txtDescription, comboStatus, comboPriority, 
                       comboCategory, txtRequester, txtAssignee, txtCreatedAt, txtResolvedAt, 
                       txtResolvedBy, txtResolution);

        layout.add(formLayout);
        return layout;
    }

    private VerticalLayout createCommentsLayout() {
        VerticalLayout layout = new VerticalLayout();
        layout.setPadding(false);
        layout.setSpacing(false);
        layout.setSizeFull();

        TextArea txtNewComment = new TextArea(getMessage("ticket_dialog.new_comment"));
        txtNewComment.setPlaceholder(getMessage("ticket_dialog.new_comment_placeholder"));
        txtNewComment.setMinHeight("60px");
        txtNewComment.setWidthFull();
        layout.add(txtNewComment);

        Button addCommentBtn = new Button(getMessage("ticket_dialog.add_comment"), e -> {
            if (txtNewComment.getValue() != null && !txtNewComment.getValue().isBlank()) {
                CommentItem comment = CommentItem.builder()
                        .content(txtNewComment.getValue())
                        .author("Admin")
                        .isInternal(false)
                        .build();
                ticketService.addComment(ticket.getId(), comment);
                txtNewComment.clear();
                layout.remove(layout.getComponentAt(layout.getComponentCount() - 1));
                layout.add(createCommentsList());
                Notification notification = Notification.show(getMessage("ticket_dialog.comment_added"));
                notification.addThemeVariants(NotificationVariant.LUMO_SUCCESS);
            }
        });
        addCommentBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        layout.add(addCommentBtn);

        layout.add(createCommentsList());
        layout.setFlexGrow(1, layout.getComponentAt(layout.getComponentCount() - 1));

        return layout;
    }

    private VerticalLayout createCommentsList() {
        VerticalLayout layout = new VerticalLayout();
        layout.setPadding(false);
        layout.setSpacing(false);
        layout.setSizeFull();

        List<CommentItem> comments = ticketService.getComments(ticket.getId());
        
        VerticalLayout commentsContainer = new VerticalLayout();
        commentsContainer.setPadding(false);
        commentsContainer.setSpacing(false);
        commentsContainer.setWidthFull();

        if (!comments.isEmpty()) {
            for (CommentItem comment : comments) {
                VerticalLayout commentLayout = new VerticalLayout();
                commentLayout.setPadding(true);
                commentLayout.setSpacing(false);
                commentLayout.setClassName("comment-item");

                TextField commentAuthor = new TextField();
                commentAuthor.setValue(comment.getAuthor());
                commentAuthor.setReadOnly(true);
                commentAuthor.addClassName("comment-author");

                TextField commentDate = new TextField();
                commentDate.setValue(Utils.formatLocalDateTime(comment.getCreatedAt()));
                commentDate.setReadOnly(true);
                commentDate.addClassName("comment-date");

                HorizontalLayout header = new HorizontalLayout(commentAuthor, commentDate);
                header.setWidthFull();
                header.setJustifyContentMode(com.vaadin.flow.component.orderedlayout.FlexComponent.JustifyContentMode.BETWEEN);

                TextArea commentContent = new TextArea();
                commentContent.setValue(comment.getContent());
                commentContent.setReadOnly(true);
                commentContent.setMinHeight("40px");
                commentContent.addClassName("comment-content");

                commentLayout.add(header, commentContent);
                commentsContainer.add(commentLayout);
            }
        }

        return commentsContainer;
    }

}
