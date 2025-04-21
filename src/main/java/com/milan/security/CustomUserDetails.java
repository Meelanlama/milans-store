package com.milan.security;

import com.milan.model.SiteUser;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class CustomUserDetails implements UserDetails {

    private SiteUser siteUser;

    public CustomUserDetails(SiteUser siteUser) {
        super();
        this.siteUser = siteUser;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {

        ArrayList<SimpleGrantedAuthority> authority = new ArrayList<>();
        siteUser.getRoles().forEach(role -> {
            authority.add(new SimpleGrantedAuthority("ROLE_" + role.getName()));
        });

        return authority;
    }

    public SiteUser getSiteUser() {
        return siteUser;
    }

    public void setSiteUser(SiteUser siteUser) {
        this.siteUser = siteUser;
    }

    @Override
    public String getPassword() {
        return siteUser.getPassword();
    }

    @Override
    public String getUsername() {
        return siteUser.getEmail();
    }

    // Add this method to expose user ID
    public Integer getUserId() {
        return siteUser.getId();
    }
}
