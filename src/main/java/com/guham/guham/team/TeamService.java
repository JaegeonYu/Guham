package com.guham.guham.team;

import com.guham.guham.domain.Account;
import com.guham.guham.domain.Team;
import com.guham.guham.team.form.TeamDescriptionForm;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
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

    public Team getTeamToUpdate(Account account, String path) {
        Team team = this.getTeam(path);

        if(!account.isManagerOf(team)){
            throw new AccessDeniedException("해당 기능을 사용할 수 없습니다.");
        }

        return team;
    }

    public Team getTeam(String path) {
        Team team = teamRepository.findByPath(path);

        if(team == null){
            throw new IllegalArgumentException(path +"에 해당하는 팀이 없습니다.");
        }

        return team;
    }

    public void updateTeamDescription(Team team, TeamDescriptionForm teamDescriptionForm) {
        team.updateDescription(teamDescriptionForm);
    }
}
