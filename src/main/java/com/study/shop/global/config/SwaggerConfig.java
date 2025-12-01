package com.study.shop.global.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Configuration
//@Profile("dev")
public class SwaggerConfig {
    private static final String SECURITY_SCHEME_NAME = "bearerAuth";

    @Bean
    public OpenAPI api() {
        return new OpenAPI()
                .info(new Info()
                        .title("Shop API")
                        .description("악기 쇼핑몰 B2C 백엔드 API 문서")
                        .version("0.1")
                        .contact(new Contact().name("Shop Team").email("support@shop.example"))
                        .license(new License().name("SHOP License")))
                .addServersItem(new Server().url("http://localhost:8080").description("Local"))
                .addSecurityItem(new SecurityRequirement().addList(SECURITY_SCHEME_NAME))

                // Swagger UI 상단의 Authorize 버튼에 Bearer <token> 입력 가능.
                // 모든 엔드포인트에 전역 적용하려면 addSecurityItem을 유지(특정 엔드포인트만 적용하려면 제거하고 컨트롤러/메서드에 어노테이션 사용).
//                .addSecurityItem(new SecurityRequirement().addList(SECURITY_SCHEME_NAME));
        ;

    }
}
