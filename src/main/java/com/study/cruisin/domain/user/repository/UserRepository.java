package com.study.cruisin.domain.user.repository;

import com.study.cruisin.domain.user.dto.UserDto;
import com.study.cruisin.domain.user.entity.User;
import jakarta.persistence.LockModeType;
import jakarta.persistence.QueryHint;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long>, UserRepositoryCustom {
    List<User> findByUsernameAndAgeGreaterThan(String username, int age);

    @Query("select m from User m where m.username = :username and m.age = :age")
    List<User> findUser(@Param("username") String username, @Param("age") int age);

    @Query("select m.username from User m")
    List<String> findUsernameList();

    @Query("select new com.study.cruisin.dto.USerDto(m.id, m.username, t.name) " +
            "from User m join m.team t")
    List<UserDto> findMemberDto();

    @Query("select m from User m where m.username in :names")
    List<User> findByNames(@Param("names") Collection<String> names);

    List<User> findListByUsername(String username);

    User findMemberByUsername(String username);
    Optional<User> findOptionalByUsername(String username);

    @Query(value = "select m from User m left join m.team t",
            countQuery = "select count(m.username) from User m")
    Page<User> findByAge(int age, Pageable pageable);

    @Modifying(clearAutomatically = true) // executeUpdate
    @Query("update User m set m.age = m.age + 1 where m.age >= :age")
    int bulkAgePlus(@Param("age") int age);

    @Query("select m from User m left join fetch m.team")
    List<User> findMemberFetchJoin();

    @Override
    @EntityGraph(attributePaths = {"team"})
    List<User> findAll();


    @EntityGraph(attributePaths = {"team"})
    @Query("select m from User m")
    List<User> findMemberEntityGraph();

    @QueryHints(value = @QueryHint(name = "org.hibernate.readOnly", value = "true"))
    User findReadOnlyByUsername(String username);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    List<User> findLockByUsername(String username);


}
