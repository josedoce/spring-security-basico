package com.sbs.learning.model.repository;

import com.sbs.learning.model.entity.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RoleRepository extends  JpaRepository<Role, Long>{
    
}
