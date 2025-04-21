package com.milan.security;

import com.milan.model.SiteUser;
import com.milan.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {

        SiteUser user = userRepository.findByEmail(username).orElse(null);

        if(user == null) {
            throw new UsernameNotFoundException("Invalid email or password");
        }

        return new CustomUserDetails(user);
    }

}
