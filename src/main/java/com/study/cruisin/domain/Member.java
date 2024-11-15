package com.study.cruisin.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;

@Entity
public class Member {
    @Id
    private Long memberId;
    // data jpa, gradle dependency
}
