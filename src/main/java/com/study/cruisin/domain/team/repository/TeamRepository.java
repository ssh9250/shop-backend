package com.study.cruisin.domain.team.repository;

import com.study.cruisin.domain.team.entity.Team;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TeamRepository extends JpaRepository<Team, Long> {

}
