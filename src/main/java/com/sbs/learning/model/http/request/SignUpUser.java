package com.sbs.learning.model.http.request;

import lombok.Data;
import org.hibernate.validator.constraints.Length;

@Data
public class SignUpUser {
    
    @Length(min = 4, max = 32)
    private String username;
    
     @Length(min = 4, max = 16)
    private String password;
}
