package com.guham.guham.modules.team;

import com.guham.guham.modules.tag.Tag;
import com.guham.guham.modules.zone.Zone;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;

@Transactional(readOnly = true)
public interface TeamRepositoryExtension {
    Page<Team> findByKeyword(String keyword, Pageable pageable);
    List<Team> findByAccount(Set<Tag> tags, Set<Zone> zones);
}
