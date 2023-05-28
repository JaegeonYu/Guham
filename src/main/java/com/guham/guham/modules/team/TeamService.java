package com.guham.guham.modules.team;

import com.guham.guham.modules.account.Account;
import com.guham.guham.modules.tag.Tag;
import com.guham.guham.modules.team.event.TeamCreatedEvent;
import com.guham.guham.modules.team.form.TeamDescriptionForm;
import com.guham.guham.modules.zone.Zone;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class TeamService {
    private final TeamRepository teamRepository;
    private final ApplicationEventPublisher eventPublisher;

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

    public Team getTeamToUpdateStatus(Account account, String path) {
        Team team = teamRepository.findTeamWithManagersByPath(path);
        checkIfManager(account, team);
        checkIfExistingTeam(team, path);
        return team;
    }

    private void checkIfManager(Account account, Team team) {
        if(!team.isManaged(account)){
            throw new AccessDeniedException("해당 기능을 사용할 수 없습니다.");
        }
    }

    public Team getTeam(String path) {
        Team team = teamRepository.findByPath(path);
        checkIfExistingTeam(team, path);
        return team;
    }

    private void checkIfExistingTeam(Team team, String path) {
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


    public void publish(Team team) {
        team.publish();
        eventPublisher.publishEvent(new TeamCreatedEvent(team));
    }

    public void close(Team team) {
        team.close();
    }

    public void startRecruit(Team team) {
        team.startRecruit();
    }

    public void stopRecruit(Team team) {
        team.stopRecruit();
    }

    public boolean isValidPath(String newPath) {
        if(teamRepository.existsByPath(newPath)){
            return false;
        }
        return true;
    }

    public void updateStudyPath(Team team, String newPath) {
        team.updatePath(newPath);
    }

    public boolean isValidTitle(String newTitle) {
        if(teamRepository.existsByTitle(newTitle)){
            return false;
        }
        return true;
    }

    public void updateStudyTitle(Team team, String newTitle) {
        team.updateTitle(newTitle);
    }

    public void remove(Team team) {
        if(team.isRemovable()){
            teamRepository.delete(team);
        }else throw new IllegalArgumentException("팀을 삭제할 수 없습니다.");
    }

    public void addMember(Team team, Account account) {
        team.addMember(account);
    }

    public void removeMember(Team team, Account account) {
        team.removeMember(account);
    }

    public Team getTeamToEnroll(String path) {
        Team team = teamRepository.findTeamOnlyByPath(path);
        checkIfExistingTeam(team, path);
        return team;
    }
}
