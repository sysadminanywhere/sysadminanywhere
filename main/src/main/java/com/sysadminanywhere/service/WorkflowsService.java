package com.sysadminanywhere.service;

import com.sysadminanywhere.client.N8nClient;
import com.sysadminanywhere.model.workflow.*;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class WorkflowsService {

    private final N8nClient n8nClient;

    public WorkflowsService(N8nClient n8nClient) {
        this.n8nClient = n8nClient;
    }

    public Page<Workflow> getAll(Pageable pageable, String filters) {
        Object result = n8nClient.getWorkflows();

        if (result instanceof ErrorResponse) {
            Notification notification = Notification.show(((ErrorResponse) result).getMessage());
            notification.addThemeVariants(NotificationVariant.LUMO_ERROR);
            return null;
        }

        List<Workflow> list = ((WorkflowListResponse) result).getData();
        return new PageImpl<>(list, pageable, list.size());
    }

    public WorkflowData getWorkflow(String workflowId) {
        Object result = n8nClient.getWorkflow(workflowId);

        if (result instanceof ErrorResponse) {
            Notification notification = Notification.show(((ErrorResponse) result).getMessage());
            notification.addThemeVariants(NotificationVariant.LUMO_ERROR);
            return null;
        }

        return (WorkflowData) result;
    }

    public List<Execution> getExecutions(String workflowId, Integer limit) {
        Object result = n8nClient.getExecutions(workflowId, limit);

        if (result instanceof ErrorResponse) {
            Notification notification = Notification.show(((ErrorResponse) result).getMessage());
            notification.addThemeVariants(NotificationVariant.LUMO_ERROR);
            return null;
        }

        return ((ExecutionListResponse) result).getData();
    }

    public void delete(String workflowId) {
        n8nClient.deleteWorkflow(workflowId);
    }

    public boolean ping() {
        try {
            Object result = n8nClient.getWorkflows();
            if (result instanceof ErrorResponse) {
                return false;
            }
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public String executeWorkflow(String workflowId) {
        Object result = n8nClient.executeWorkflow(workflowId);

        if (result instanceof ErrorResponse) {
            Notification notification = Notification.show(((ErrorResponse) result).getMessage());
            notification.addThemeVariants(NotificationVariant.LUMO_ERROR);
            return null;
        }

        return result.toString();
    }

}