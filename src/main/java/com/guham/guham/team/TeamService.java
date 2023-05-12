package com.guham.guham.team;

import com.guham.guham.domain.Account;
import com.guham.guham.domain.Team;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class TeamService {
    private final TeamRepository teamRepository;

    public Team createTeam(Team team, Account account) {
        Team newTeam = teamRepository.save(team);
        newTeam.addManager(account);
        return newTeam;
    }
}
