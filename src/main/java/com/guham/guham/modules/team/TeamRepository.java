package com.guham.guham.modules.team;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;

@Transactional(readOnly = true)
public interface TeamRepository extends JpaRepository<Team, Long> {
    boolean existsByPath(String path);

    // findByPath 발생 시 Team의 member, manager, zones, tags 모두  필요
    @EntityGraph(value = "Team.withAll", type = EntityGraph.EntityGraphType.LOAD)
    Team findByPath(String path);
    @EntityGraph(value = "Team.withTagsAndManager", type = EntityGraph.EntityGraphType.FETCH)
    Team findTeamWithTagsByPath(String path);

    @EntityGraph(value = "Team.withZonesAndManager", type = EntityGraph.EntityGraphType.FETCH)
    Team findTeamWithZonesByPath(String path);

    @EntityGraph(value = "Team.withManager", type = EntityGraph.EntityGraphType.FETCH)
    Team findTeamWithManagersByPath(String path);

    @EntityGraph(value = "Team.withMember", type = EntityGraph.EntityGraphType.FETCH)
    Team findTeamWithMembersByPath(String path);

    boolean existsByTitle(String title);

    Team findTeamOnlyByPath(String path);
}
