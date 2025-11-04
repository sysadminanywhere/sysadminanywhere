package com.sysadminanywhere.directory.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
public class KafkaTopic {

    public static String DIRECTORY_REQUEST = "directory-request";
    public static String DIRECTORY_RESPONSE = "directory-response";

    @Bean
    public NewTopic topicDirectoryRequest() {
        return TopicBuilder.name(DIRECTORY_REQUEST).build();
    }

    @Bean
    public NewTopic topicDirectoryResponse() {
        return TopicBuilder.name(DIRECTORY_RESPONSE).build();
    }

}