package com.study.shop.global.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Configuration
//@Profile("dev")
public class SwaggerConfig {
    @Bean
    public OpenAPI shopOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Shop API")
                        .description("악기 쇼핑몰 B2C 백엔드 API 문서")
                        .version("1.0")
                        .contact(new Contact().name("Shop Team").email("support@shop.example"))
                        .license(new License().name("SHOP License")))
                .addServersItem(new Server().url("http://localhost:8080").description("Local"));
    }
}
