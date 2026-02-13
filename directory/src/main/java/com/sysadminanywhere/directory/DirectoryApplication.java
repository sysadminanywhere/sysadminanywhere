package com.sysadminanywhere.directory;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;

@SpringBootApplication(exclude = { DataSourceAutoConfiguration.class })
public class DirectoryApplication {

	public static void main(String[] args) {
		SpringApplication.run(DirectoryApplication.class, args);
	}

}
