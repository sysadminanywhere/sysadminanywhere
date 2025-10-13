package com.sysadminanywhere.workflows;

import org.camunda.bpm.spring.boot.starter.annotation.EnableProcessApplication;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@EnableProcessApplication
public class WorkflowsApplication {

	public static void main(String[] args) {
		SpringApplication.run(WorkflowsApplication.class, args);
	}

}
