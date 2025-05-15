package com.recap.Recap.Repos;

import com.recap.Recap.Entity.AssemblyAiEntity;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AssemblyAiRepo extends JpaRepository< AssemblyAiEntity,Long> {

    @EntityGraph(attributePaths = "transcribeResult")
    List<AssemblyAiEntity> findAll();
}
