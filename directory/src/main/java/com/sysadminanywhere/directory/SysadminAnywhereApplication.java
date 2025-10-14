package com.sysadminanywhere.directory;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.cloud.vault.config.VaultAutoConfiguration;
import org.springframework.cloud.vault.config.VaultHealthIndicatorAutoConfiguration;

@SpringBootApplication(exclude = { DataSourceAutoConfiguration.class })
public class SysadminAnywhereApplication {

	public static void main(String[] args) {
		SpringApplication.run(SysadminAnywhereApplication.class, args);
	}

}
