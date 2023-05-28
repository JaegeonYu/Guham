package com.guham.guham.modules.team;

import com.guham.guham.modules.account.Account;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class TeamFactory {
    private final TeamRepository teamRepository;
    private final TeamService teamService;

    public Team createTeam(String path, Account manager){
        Team team = Team.builder()
                .path(path)
                .title("team title")
                .shortDescription("short Description")
                .fullDescription("full Description")
                .build();
        teamService.createTeam(team, manager);
        return team;
    }
}
