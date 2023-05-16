package com.guham.guham.team;

import com.guham.guham.domain.Account;
import com.guham.guham.domain.Tag;
import com.guham.guham.domain.Team;
import com.guham.guham.domain.Zone;
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

        checkIfManager(account, team);

        return team;
    }
    public Team getTeamToUpdateTag(Account account, String path){
        Team team = teamRepository.findTeamWithTagsByPath(path);
        checkIfExistingTeam(team, path);
        checkIfManager(account, team);
        return team;
    }

    public Team getTeamToUpdateZone(Account account, String path){
        Team team = teamRepository.findTeamWithZonesByPath(path);
        checkIfExistingTeam(team, path);
        checkIfManager(account, team);
        return team;
    }

    private static void checkIfManager(Account account, Team team) {
        if(!account.isManagerOf(team)){
            throw new AccessDeniedException("해당 기능을 사용할 수 없습니다.");
        }
    }

    public Team getTeam(String path) {
        Team team = teamRepository.findByPath(path);
        checkIfExistingTeam(team, path);

        return team;
    }

    private static void checkIfExistingTeam(Team team, String path) {
        if(team == null){
            throw new IllegalArgumentException(path +"에 해당하는 팀이 없습니다.");
        }
    }

    public void updateTeamDescription(Team team, TeamDescriptionForm teamDescriptionForm) {
        team.updateDescription(teamDescriptionForm);
    }

    public void enableTeamBanner(Team team) {
        team.enableBanner();
    }

    public void disableTeamBanner(Team team) {
        team.disableBanner();
    }

    public void updateTeamBanner(Team team, String image) {
        team.updateTeamBanner(image);
    }

    public void addTag(Team team, Tag tag) {
        team.getTags().add(tag);
    }

    public void removeTag(Team team, Tag tag) {
        team.getTags().remove(tag);
    }

    public void addZone(Team team, Zone zone) {
        team.getZones().add(zone);
    }

    public void removeZone(Team team, Zone zone) {
        team.getZones().remove(zone);
    }
}
