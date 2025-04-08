package com.MobilePrepaidRecharge.app.service;

import com.MobilePrepaidRecharge.app.model.Admin;
import com.MobilePrepaidRecharge.app.repository.AdminRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.*;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Service;
import java.util.Collections;

@Service
public class AdminService implements UserDetailsService {

    @Autowired
    private AdminRepository adminRepository;
    @Autowired
    private PasswordEncoder passwordEncoder; 
    
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Admin admin = adminRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("Admin not found"));
        return new org.springframework.security.core.userdetails.User(
            admin.getUsername(), admin.getPassword(),
            Collections.singleton(new SimpleGrantedAuthority("ROLE_" + admin.getRole()))
        );
    }
    
    public Admin registerAdmin(Admin admin) {
        if(adminRepository.findByUsername(admin.getUsername()).isPresent()){
            throw new RuntimeException("Admin already exists");
        }
        // Set default role and encrypt password
        admin.setRole("ADMIN");
        admin.setPassword(passwordEncoder.encode(admin.getPassword()));
        return adminRepository.save(admin);
    }
}
