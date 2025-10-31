package com.vres.config;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.vres.entity.ProjectUser;
import com.vres.entity.Users;
import com.vres.repository.ProjectUserRepository;
import com.vres.repository.UsersRepository;

@Service
public class UserDetailsServiceImpl implements UserDetailsService {

    @Autowired
    private UsersRepository usersRepository;
    
    @Autowired
    private ProjectUserRepository projectUserRepository; // Inject this

    @Override
    @Transactional(readOnly = true) // Important for loading related entities
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        // 1. Load the user
        Users user = usersRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + email));

        // 2. Load all their project assignments
        List<ProjectUser> userAssignments = projectUserRepository.findAllByUserId(user.getId());

        // 3. Convert assignments into a Set of "ROLE_" strings
        Set<GrantedAuthority> authorities = userAssignments.stream()
                .filter(assignment -> assignment.getRole() != null && assignment.getRole().getName() != null)
                .map(assignment -> new SimpleGrantedAuthority("ROLE_" + assignment.getRole().getName().toUpperCase()))
                .collect(Collectors.toSet());
                
        // 4. Add a default role if they have no assignments (or keep empty)
        if (authorities.isEmpty()) {
            authorities.add(new SimpleGrantedAuthority("ROLE_USER")); // Or just leave empty
        }

        // 5. Return the new CustomUserDetails object
        return new CustomUserDetails(user, authorities);
    }
}