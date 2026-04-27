package com.study.shop.global.aop;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.hibernate.Session;
import org.springframework.context.annotation.Configuration;

@Configuration
@Aspect
public class SoftDeleteAspect {
    @PersistenceContext
    private EntityManager entityManager;

    @Before("execution(* com.study.shop.repository..*.find*(..)")
    public void enableFilter() {
        Session session = entityManager.unwrap(Session.class);
        session.enableFilter("softDeleteFilter");
    }
}
