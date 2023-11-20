package com.gon.fitness.web.study;

import lombok.Data;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.NotBlank;

@Data
public class StudyForm {

    @NotBlank
    @Length(min = 2, max = 10)
    private String path;

    @Length(max = 50)
    private String title;

    private String shortDescription;

    private String fullDescription;



}
