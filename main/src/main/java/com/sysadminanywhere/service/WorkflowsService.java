package com.sysadminanywhere.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sysadminanywhere.client.N8nClient;
import com.sysadminanywhere.model.workflow.*;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;

import java.util.List;

@Service
public class WorkflowsService {

    private final N8nClient n8nClient;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public WorkflowsService(N8nClient n8nClient) {
        this.n8nClient = n8nClient;
    }

    public Page<Workflow> getAll(Pageable pageable, String filters) {
        try {
            WorkflowListResponse response = n8nClient.getWorkflows();
            List<Workflow> list = response.getData();
            return new PageImpl<>(list, pageable, list.size());
        } catch (HttpClientErrorException e) {
            handleErrorResponse(e.getResponseBodyAsString());
            return Page.empty();
        } catch (Exception e) {
            showError(e.getMessage());
            return Page.empty();
        }
    }

    public WorkflowData getWorkflow(String workflowId) {
        try {
            WorkflowData workflowData = n8nClient.getWorkflow(workflowId);
            return workflowData;
        } catch (HttpClientErrorException e) {
            handleErrorResponse(e.getResponseBodyAsString());
            return null;
        } catch (Exception e) {
            showError(e.getMessage());
            return null;
        }
    }

    public List<Execution> getExecutions(String workflowId, Integer limit) {
        try {
            return n8nClient.getExecutions(workflowId, limit).getData();
        } catch (HttpClientErrorException e) {
            handleErrorResponse(e.getResponseBodyAsString());
            return List.of();
        } catch (Exception e) {
            showError(e.getMessage());
            return List.of();
        }
    }

    public void delete(String workflowId) {
        n8nClient.deleteWorkflow(workflowId);
    }

    public boolean ping() {
        try {
            n8nClient.getWorkflows();
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public String executeWorkflow(String workflowId) {
        try {
            return n8nClient.executeWorkflow(workflowId);
        } catch (HttpClientErrorException e) {
            handleErrorResponse(e.getResponseBodyAsString());
            return null;
        } catch (Exception e) {
            showError(e.getMessage());
            return null;
        }
    }

    private void handleErrorResponse(String responseBody) {
        try {
            ErrorResponse error = objectMapper.readValue(responseBody, ErrorResponse.class);
            showError(error.getMessage());
        } catch (Exception e) {
            showError(responseBody);
        }
    }

    private void showError(String message) {
        Notification notification = Notification.show(message, 5000, Notification.Position.MIDDLE);
        notification.addThemeVariants(NotificationVariant.LUMO_ERROR);
    }

}