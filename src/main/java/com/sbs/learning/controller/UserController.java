package com.sbs.learning.controller;

import java.util.UUID;

import javax.servlet.http.HttpServletRequest;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import com.sbs.learning.error.exception.ApiException;
import com.sbs.learning.model.entity.User;
import com.sbs.learning.model.http.json.UserJSON;
import com.sbs.learning.model.repository.UserRepository;
import com.sbs.learning.utils.jwt.Payload;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;

@RestController
@Validated
public class UserController {
    
    private final UserRepository userRepository;
    public UserController(UserRepository ur){
        this.userRepository = ur;
    }
    
    @GetMapping("/users")
    public List<UserJSON> allUsers(){
        return this.userRepository.findAll().stream().map((User user) -> {
            return user.toJson();
        }).collect(Collectors.toList());
    }
    
    @DeleteMapping("/users/delete/{uuid}")
    public ResponseEntity<Object> deletarUsuario(HttpServletRequest request, @PathVariable String uuid) throws ApiException {
        Payload payload = (Payload) request.getAttribute("user");
       
        
        if(!uuid.equals(payload.getUuid()) ){
             throw new ApiException("Uuid is incompatible", HttpStatus.BAD_REQUEST);
        }
        
         Optional<User> user = this.userRepository.findById(UUID.fromString(payload.getUuid()));
        if(!user.isPresent()){
            throw new ApiException("User not found", HttpStatus.NOT_FOUND);
        }
        
        this.userRepository.delete(user.get());
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/users/admin")
    public ResponseEntity<Object> getAdminInfo(HttpServletRequest request) {
        return this.informations(request, "Welcome admin");
    }
    
    @GetMapping("/users/manager")
    public ResponseEntity<Object> getManagerInfo(HttpServletRequest request) {
        return this.informations(request, "Welcome manager");
    }
    
    @GetMapping("/users/user")
    public ResponseEntity<Object> getUserInfo(HttpServletRequest request) {
        return this.informations(request, "Welcome user");
    }
    
    private ResponseEntity<Object> informations(HttpServletRequest request, String greeting){
        Payload payload = (Payload) request.getAttribute("user");
        Map<String, Object> map = new HashMap<>();
        map.put("jwt_info", payload);
        map.put("greeting", greeting+" "+payload.getUsername());
        return  ResponseEntity.ok(map);
    }
}
