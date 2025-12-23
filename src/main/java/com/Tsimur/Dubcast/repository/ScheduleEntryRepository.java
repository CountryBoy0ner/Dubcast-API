package com.Tsimur.Dubcast.repository;

import com.Tsimur.Dubcast.model.ScheduleEntry;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ScheduleEntryRepository extends JpaRepository<ScheduleEntry, Long> {
}