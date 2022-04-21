package com.sbs.learning.model.entity;

import com.sbs.learning.model.http.json.RoleJSON;
import com.sbs.learning.model.http.json.UserJSON;
import java.util.Set;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import org.springframework.security.core.GrantedAuthority;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Entity
@Table(name = "roles")
@NoArgsConstructor
public class Role implements GrantedAuthority {

    public static final String USER = "USER";
    public static final String MANAGER = "MANAGER";
    public static final String ADMIN = "ADMIN";

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    private String authority;
    
    @ManyToOne
    @JoinColumn(name="user_uuid")
    private User user;
    
    public Role(String authority) {
        this.authority = authority;
    }
    
    public RoleJSON toJson(){
        UserJSON userJson = new UserJSON();
        RoleJSON roleJson = new RoleJSON();
        
        String[] authorities = null;
        if(this.getUser() != null){
            userJson.setUsername(this.getUser().getUsername());
            userJson.setUuid(this.getUser().getUuid().toString());
        } 
        userJson.setAuthorities(authorities);
        roleJson.setUser(userJson);
        roleJson.setAuthority(this.getAuthority());
        return roleJson;
    }
}
