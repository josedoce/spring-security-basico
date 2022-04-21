package com.sbs.learning.controller;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import javax.transaction.Transactional;
import javax.validation.Valid;

import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.http.HttpStatus;

import com.sbs.learning.model.entity.Role;
import com.sbs.learning.model.entity.User;
import com.sbs.learning.model.http.request.SignInUser;
import com.sbs.learning.model.http.request.SignUpUser;
import com.sbs.learning.model.http.response.ApiResponse;
import com.sbs.learning.model.repository.UserRepository;
import com.sbs.learning.utils.jwt.JwtManager;
import com.sbs.learning.utils.jwt.Payload;


@RestController
public class AuthController {
    private AuthenticationManager authenticationManager;
    private UserRepository userRepository;

    public AuthController(AuthenticationManager am, UserRepository ur) {
        this.authenticationManager = am;
        this.userRepository = ur;
    }

    @PostMapping("/signup/admin")
    public ResponseEntity<ApiResponse> signUpAsAdmin(@RequestBody @Valid SignUpUser user) {
        Role roleAdmin = new Role(Role.ADMIN);
        Set<Role> roles = new HashSet<>();
        roles.add(roleAdmin);
        return this.signUpUserBasedInType(user, roles);
    }

    @PostMapping("/signup/manager")
    public ResponseEntity<ApiResponse> signUpAsManager(@RequestBody @Valid SignUpUser user) {
        Role roleManager = new Role(Role.MANAGER);
        Set<Role> roles = new HashSet<>();
        roles.add(roleManager);
        return this.signUpUserBasedInType(user, roles);
    }

    @PostMapping("/signup/user")
    public ResponseEntity<ApiResponse> signUpAsUser(@RequestBody @Valid SignUpUser user) {
        Role roleUser = new Role(Role.USER);
        Set<Role> roles = new HashSet<>();
        roles.add(roleUser);
        return this.signUpUserBasedInType(user, roles);
    }

    @PostMapping("/signin")
    public ResponseEntity<ApiResponse> signIn(@RequestBody @Valid SignInUser user) {
      
        this.setUserInContext(this.authenticate(user.getUsername(), user.getPassword()));
        Optional<User> findedUser = this.userRepository.findByUsername(user.getUsername());

        JwtManager jm = new JwtManager();
        Map<String, Object> claims = new HashMap<>();
        Payload payload = new Payload();
        payload.setUsername(user.getUsername());
        Set<Role> roles = findedUser.get().getAuthorities();
        String[] authorities = new String[roles.size()];
        int i = 0;

        for (Role role : roles) {
            authorities[i] = role.getAuthority();
            i++;
        }
        payload.setUuid(findedUser.get().getUuid().toString());
        payload.setAuthorities(authorities);

        claims.put("user", payload);

        String token = jm.generateToken(claims, "user");
        return ResponseEntity.ok(new ApiResponse(200, "success", token));
    }

    private ResponseEntity<ApiResponse> signUpUserBasedInType(SignUpUser user, Set<Role> roles) {
        User userEntity = new User();

        Optional<User> hasUser = this.userRepository.findByUsername(user.getUsername());
        if (hasUser.isPresent()) {
            return ResponseEntity.badRequest().body(new ApiResponse(400, "error: User already exists."));
        }
        userEntity.setUsername(user.getUsername());
        userEntity.setPassword(user.getPassword());
        roles.forEach((role) -> {
            role.setUser(userEntity);
          
        });
        
        userEntity.setAuthorities(roles);
        
        User userCreated = this.userRepository.save(userEntity);
        if (userCreated == null) {
            return ResponseEntity.badRequest().body(new ApiResponse(400, "error: Is not created.", ""));

        }

        this.setUserInContext(this.authenticate(user.getUsername(), user.getPassword()));
        JwtManager jm = new JwtManager();
        Map<String, Object> claims = new HashMap<>();
        Payload payload = new Payload();
        payload.setUsername(user.getUsername());
        Set<Role> authorities = userEntity.getAuthorities();
        String[] arrayAuthorities = new String[authorities.size()];
        int i = 0;

        for (Role authoritie : authorities) {
            arrayAuthorities[i] = authoritie.getAuthority();
            i++;
        }
        payload.setUuid(userEntity.getUuid().toString());
        payload.setAuthorities(arrayAuthorities);

        claims.put("user", payload);

        String token = jm.generateToken(claims, "user");
        return ResponseEntity.status(HttpStatus.CREATED).body(new ApiResponse(203, "success", token));
    }

    @Transactional
    private Authentication authenticate(String username, String password) {
        return authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(username, password)
        );
    }

    private void setUserInContext(Authentication authentication) {
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }
}
