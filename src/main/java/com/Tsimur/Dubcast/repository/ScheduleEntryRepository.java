package com.Tsimur.Dubcast.repository;

import com.Tsimur.Dubcast.model.ScheduleEntry;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

public interface ScheduleEntryRepository extends JpaRepository<ScheduleEntry, Long> {

    @Query("""
            select case when count(e) > 0 then true else false end
            from ScheduleEntry e
            where e.startTime < :end
              and e.endTime > :start
            """)
    boolean existsOverlap(@Param("start") OffsetDateTime start,
                          @Param("end") OffsetDateTime end);


    @Query("""
            select max(e.endTime)
            from ScheduleEntry e
            where e.endTime > :from
            """)
    Optional<OffsetDateTime> findLastEndTimeAfter(@Param("from") OffsetDateTime from);




    @Query("""
       select e
       from ScheduleEntry e
       where e.startTime <= :now and e.endTime > :now
       """)
    Optional<ScheduleEntry> findCurrent(@Param("now") OffsetDateTime now);

    @Query("""
       select e
       from ScheduleEntry e
       where e.startTime > :now
       order by e.startTime asc
       """)
    List<ScheduleEntry> findNext(@Param("now") OffsetDateTime now, Pageable pageable);

}