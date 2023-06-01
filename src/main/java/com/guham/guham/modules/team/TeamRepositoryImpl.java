package com.guham.guham.modules.team;

import com.guham.guham.modules.tag.QTag;
import com.guham.guham.modules.tag.Tag;
import com.guham.guham.modules.zone.QZone;
import com.guham.guham.modules.zone.Zone;
import com.querydsl.core.QueryResults;
import com.querydsl.jpa.JPQLQuery;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.support.QuerydslRepositorySupport;

import java.util.List;
import java.util.Set;

public class TeamRepositoryImpl extends QuerydslRepositorySupport implements TeamRepositoryExtension {
    /**
     * Creates a new {@link QuerydslRepositorySupport} instance for the given domain type.
     */
    public TeamRepositoryImpl() {
        super(Team.class);
    }

    @Override
    public Page<Team> findByKeyword(String keyword, Pageable pageable) {
        QTeam team = QTeam.team;

        JPQLQuery<Team> query = from(team).where(team.published.isTrue()
                        .and(team.title.containsIgnoreCase(keyword))
                        .or(team.tags.any().title.containsIgnoreCase(keyword))
                        .or(team.zones.any().localNameOfCity.containsIgnoreCase(keyword)))
                .leftJoin(team.tags, QTag.tag).fetchJoin()
                .leftJoin(team.zones, QZone.zone).fetchJoin()
                .distinct();

        JPQLQuery<Team> teamJPQLQuery = getQuerydsl().applyPagination(pageable, query);
        QueryResults<Team> fetchResults = teamJPQLQuery.fetchResults();

        return new PageImpl<>(fetchResults.getResults(), pageable, fetchResults.getTotal());
    }

    @Override
    public List<Team> findByAccount(Set<Tag> tags, Set<Zone> zones) {
        QTeam team = QTeam.team;
        JPQLQuery<Team> query = from(team).where(team.published.isTrue()
                        .and(team.closed.isFalse())
                        .and(team.tags.any().in(tags))
                        .and(team.zones.any().in(zones)))
                .leftJoin(team.tags, QTag.tag).fetchJoin()
                .leftJoin(team.zones, QZone.zone).fetchJoin()
                .orderBy(team.publishedDateTime.desc())
                .distinct()
                .limit(9);
        return query.fetch();
    }
}
