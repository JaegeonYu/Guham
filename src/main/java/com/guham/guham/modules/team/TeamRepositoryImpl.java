package com.guham.guham.modules.team;

import com.guham.guham.modules.account.QAccount;
import com.guham.guham.modules.tag.QTag;
import com.guham.guham.modules.zone.QZone;
import com.querydsl.core.QueryResults;
import com.querydsl.jpa.JPQLQuery;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.support.QuerydslRepositorySupport;

import java.util.List;

public class TeamRepositoryImpl extends QuerydslRepositorySupport implements TeamRepositoryExtension{
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
                .leftJoin(team.members, QAccount.account).fetchJoin()
                .distinct();

        JPQLQuery<Team> teamJPQLQuery = getQuerydsl().applyPagination(pageable, query);
        QueryResults<Team> fetchResults = teamJPQLQuery.fetchResults();

        return new PageImpl<>(fetchResults.getResults(), pageable, fetchResults.getTotal());
    }
}
