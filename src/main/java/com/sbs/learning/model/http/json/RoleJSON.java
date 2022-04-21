package com.sbs.learning.model.http.json;

import lombok.Data;

@Data
public class RoleJSON {
    private String authority;
    private UserJSON user;
}
