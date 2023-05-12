package com.guham.guham.team;

import com.guham.guham.domain.Team;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;

@Transactional(readOnly = true)
public interface TeamRepository extends JpaRepository<Team, Long> {
    boolean existsByPath(Team team);
}
