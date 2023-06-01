package com.guham.guham.modules.team;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
@Transactional(readOnly = true)
public interface TeamRepositoryExtension {
    Page<Team> findByKeyword(String keyword, Pageable pageable);
}
