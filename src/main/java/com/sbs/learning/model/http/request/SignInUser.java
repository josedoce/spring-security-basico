package com.sbs.learning.model.http.request;

import lombok.Data;
import org.hibernate.validator.constraints.Length;

@Data
public class SignInUser {

    @Length(min = 4, max = 32)
    private String username;

    @Length(min = 4, max = 16)
    private String password;
    
    private String[] authorities;

    public String[] getAuthorities() {
        return authorities;
    }
    
}
