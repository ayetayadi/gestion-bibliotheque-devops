package com.bibliotheque.gestion_bibliotheque.security;

import java.util.Collection;               
import java.util.Collections;           

import org.springframework.security.core.GrantedAuthority; 
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import com.bibliotheque.gestion_bibliotheque.entities.user.Role;
import com.bibliotheque.gestion_bibliotheque.entities.user.Utilisateur;

public class UserDetailsImpl implements UserDetails {

    private final Utilisateur utilisateur;

    public UserDetailsImpl(Utilisateur utilisateur) {
        this.utilisateur = utilisateur;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return Collections.singletonList(
                new SimpleGrantedAuthority("ROLE_" + utilisateur.getRole().name())
        );
    }

    @Override
    public String getPassword() {
        return utilisateur.getMotDePasse();
    }

    @Override
    public String getUsername() {
        return utilisateur.getEmail();
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
public boolean isEnabled() {

    // 🔥 SUPER ADMIN : accès global
    if (utilisateur.getRole() == Role.SUPER_ADMIN) {
        return utilisateur.isActif();
    }

    // ADMIN / BIBLIOTHECAIRE : dépend de la bibliothèque
    return utilisateur.isActif()
            && utilisateur.getBibliotheque() != null
            && utilisateur.getBibliotheque().isActif();
}
}
