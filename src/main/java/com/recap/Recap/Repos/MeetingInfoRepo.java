package com.recap.Recap.Repos;

import com.recap.Recap.Entity.MeetingInfoEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MeetingInfoRepo extends JpaRepository<MeetingInfoEntity, Long> {
}
