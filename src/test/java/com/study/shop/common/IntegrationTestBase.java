package com.study.shop.common;

import com.study.shop.ShopApplication;
import com.study.shop.config.EmbeddedRedisConfig;
import jakarta.transaction.Transactional;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest(classes = ShopApplication.class)
@ActiveProfiles("test")
@Import(EmbeddedRedisConfig.class)
@AutoConfigureMockMvc
@Transactional
public class IntegrationTestBase {
}
