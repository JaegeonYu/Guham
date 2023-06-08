package com.guham.guham.modules.team;

import com.guham.guham.modules.account.Account;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Transactional(readOnly = true)
public interface TeamRepository extends JpaRepository<Team, Long> , TeamRepositoryExtension{
    boolean existsByPath(String path);

    // findByPath 발생 시 Team의 member, manager, zones, tags 모두  필요

    @EntityGraph(attributePaths = {"tags", "zones", "managers", "members"}, type = EntityGraph.EntityGraphType.LOAD)
    Team findByPath(String path);

    @EntityGraph(attributePaths = {"tags", "managers"})
    Team findTeamWithTagsByPath(String path);


    @EntityGraph(attributePaths = {"zones", "managers"})
    Team findTeamWithZonesByPath(String path);

    @EntityGraph(attributePaths = "managers")
    Team findTeamWithManagersByPath(String path);


    @EntityGraph(attributePaths = "members")
    Team findTeamWithMembersByPath(String path);

    boolean existsByTitle(String title);

    Team findTeamOnlyByPath(String path);

    @EntityGraph(attributePaths = {"tags", "zones"})
    Team findTeamWithTagsAndZonesById(Long id);

    @EntityGraph(attributePaths = {"members", "managers"})
    Team findTeamWithManagersAndMembersById(Long id);
    @EntityGraph(attributePaths = {"zones", "tags"})
    List<Team> findFirst9ByPublishedAndClosedOrderByPublishedDateTimeDesc(boolean published, boolean closed);

    // 쿼리할 때 연관관계 엔티티 참조는 N+1 나지 않음, 결과로 들고 올 때 연관관계 엔티티 들고 올 때는 N+1 해결
    List<Team> findFirst5ByManagersContainingAndClosedOrderByPublishedDateTimeDesc(Account account, boolean closed);

    List<Team> findFirst5ByMembersContainingAndClosedOrderByPublishedDateTimeDesc(Account account, boolean closed);
}
