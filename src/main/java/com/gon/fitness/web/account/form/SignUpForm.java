package com.gon.fitness.web.account.form;

import lombok.Data;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;

@Data

public class SignUpForm {

    @NotBlank
    @Email
    private String email;
    @NotBlank
    private String nickname;
    @NotBlank
    private String password;


}
