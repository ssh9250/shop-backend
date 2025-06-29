package com.study.cruisin.domain.Instrument.repository;

import com.study.cruisin.domain.Instrument.entity.Instrument;
import com.study.cruisin.domain.member.entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface InstrumentRepository extends JpaRepository<Instrument,Long> {
    List<Instrument> findBySellerId(Long sellerId);
}
