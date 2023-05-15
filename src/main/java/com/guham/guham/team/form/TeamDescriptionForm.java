package com.guham.guham.team.form;

import com.guham.guham.domain.Team;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.NotBlank;

@Data
@NoArgsConstructor
public class TeamDescriptionForm {
    @NotBlank
    @Length(max = 100)
    private String shortDescription;

    @NotBlank
    private String fullDescription;

    public TeamDescriptionForm(Team team) {
        this.shortDescription = team.getShortDescription();
        this.fullDescription = team.getFullDescription();
    }
}
