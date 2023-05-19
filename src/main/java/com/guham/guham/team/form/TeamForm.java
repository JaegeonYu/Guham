package com.guham.guham.team.form;

import com.guham.guham.domain.Team;
import lombok.Data;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;

@Data
public class TeamForm {
    @NotBlank
    @Length(min = 2, max = 20)
    @Pattern(regexp = "^[ㄱ-ㅎ가-힣a-z0-9_-]{2,20}$")
    private String path;

    @NotBlank
    @Length(max = 50)
    private String title;

    @NotBlank
    @Length(max = 100)
    private String shortDescription;
    @NotBlank
    private String fullDescription;

    public Team getTeam(){
        return Team.builder()
                .path(path)
                .title(title)
                .shortDescription(shortDescription)
                .fullDescription(fullDescription)
                .build();
    }
}
