package org.reminderservice.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI reminderServiceOpenApi() {
        return new OpenAPI().info(new Info()
                .title("Reminder Service API")
                .description("Manages reminders. Most reminders are created asynchronously " +
                        "from task-service's TaskCreated Kafka event, but the REST API remains " +
                        "fully usable on its own.")
                .version("v1"));
    }
}
