package com.guham.guham.team.validator;

import com.guham.guham.team.TeamRepository;
import com.guham.guham.team.form.TeamForm;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;
@Component
@RequiredArgsConstructor
public class TeamValidator implements Validator {
    private final TeamRepository teamRepository;

    @Override
    public boolean supports(Class<?> clazz) {
        return TeamForm.class.isAssignableFrom(clazz);
    }

    @Override
    public void validate(Object target, Errors errors) {
        TeamForm teamForm = (TeamForm) target;
        if(teamRepository.existsByPath(teamForm.getTeam())){
            errors.rejectValue("path","wrong.path", "해당 팀 경로값을 사용할 수 없습니다");
        }
    }
}
