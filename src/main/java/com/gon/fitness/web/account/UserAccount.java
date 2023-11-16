package com.gon.fitness.web.account;

import com.gon.fitness.domain.account.Account;
import lombok.Getter;
import lombok.Setter;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;

import java.util.List;

@Getter
@Setter
public class UserAccount extends User {

    private Account account;

    public UserAccount(Account account) {

        super(account.getNickname(), account.getPassword(), List.of(new SimpleGrantedAuthority("ROLE_USER")));
        this.account = account;
    }





}
