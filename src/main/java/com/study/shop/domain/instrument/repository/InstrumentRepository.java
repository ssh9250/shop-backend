package com.study.shop.domain.instrument.repository;

import com.study.shop.domain.instrument.entity.Instrument;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface InstrumentRepository extends JpaRepository<Instrument,Long> {
    List<Instrument> findBySellerId(Long sellerId);
}
