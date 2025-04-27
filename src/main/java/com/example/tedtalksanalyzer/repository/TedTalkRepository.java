package com.example.tedtalksanalyzer.repository;

import com.example.tedtalksanalyzer.model.TedTalk;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface TedTalkRepository extends JpaRepository<TedTalk, UUID>, JpaSpecificationExecutor<TedTalk> {

    Slice<TedTalk> findAllBy(Pageable pageable);
}
