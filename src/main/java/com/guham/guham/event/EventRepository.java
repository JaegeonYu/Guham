package com.guham.guham.event;

import com.guham.guham.domain.Event;
import com.guham.guham.domain.Team;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Transactional(readOnly = true)
public interface EventRepository extends JpaRepository<Event, Long> {
    @EntityGraph(value = "Event.withEnrollments")
    List<Event> findByTeamOrderByStartDateTime(Team team);
}
