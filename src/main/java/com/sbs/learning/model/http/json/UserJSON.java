package com.sbs.learning.model.http.json;

import lombok.Data;

@Data
public class UserJSON {
    private String uuid;
    private String username;
    private String[] authorities;
}
