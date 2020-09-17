package com.augusto.twofactorauthenticaton.user.role;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class RoleService {

    @Autowired
    private RoleRepository roleRepository;

    public Role findByName(String role) {
        Optional<Role> roleOptional = roleRepository.findByName(role);
        if (roleOptional.isPresent()) {
            return roleOptional.get();
        } else {
            Role r = new Role();
            r.setName(role);
            return roleRepository.save(r);
        }
    }


}
