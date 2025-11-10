package com.sysadminanywhere.service;

import com.sysadminanywhere.client.N8nClient;
import com.sysadminanywhere.model.workflow.Workflow;
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
        List<Workflow> list = n8nClient.getWorkflows().getData();

        return new PageImpl<>(list, pageable, list.size());
    }

}