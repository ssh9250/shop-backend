package com.study.shop.domain.member.repository;

import com.study.shop.domain.member.entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface MemberRepository extends JpaRepository<Member, Long> {
    // 권한 정보를 함께 fetch (N+1 방지)
    @Query("SELECT m FROM Member m LEFT JOIN FETCH m.role WHERE m.email = :email")
    Optional<Member> findByEmailWithRoles(@Param("email") String email);

    Optional<Member> findMemberByEmail(String email);
}
