package com.study.shop.domain.member.repository;

import com.study.shop.admin.dto.DeletedMemberProjection;
import com.study.shop.domain.member.dto.MemberListResponseDto;
import com.study.shop.domain.member.entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface MemberRepository extends JpaRepository<Member, Long> {
    // 권한 정보를 함께 fetch (N+1 방지)
//    @Query("SELECT m FROM Member m LEFT JOIN FETCH m.role WHERE m.email = :email")
//    Optional<Member> findByEmailWithRoles(@Param("email") String email);


//    @Query("select ")
//    List<Member> findAllMembers();

    Optional<Member> findMemberByEmail(String email);

    boolean existsByEmail(String email);

    boolean existsByNickname(String nickname);

    @Query(value = "SELECT email, nickname, role, deleted_at FROM member WHERE deleted_at IS NOT NULL", nativeQuery = true)
    List<DeletedMemberProjection> findSoftDeletedMemberList();
}
