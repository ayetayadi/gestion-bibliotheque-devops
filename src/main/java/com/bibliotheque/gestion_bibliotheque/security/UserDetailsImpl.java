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

    // ⭐ MÉTHODE ESSENTIELLE : permet aux controllers d'accéder au vrai utilisateur
    public Utilisateur getUtilisateur() {
        return utilisateur;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        // Très important : Spring exige "ROLE_XXX"
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
    if (utilisateur.getRole() == Role.SUPER_ADMIN) {
        return utilisateur.isActif();
    } else if (utilisateur.getRole() == Role.ADMIN || utilisateur.getRole() == Role.BIBLIOTHECAIRE) {
        return utilisateur.isActif()
               && utilisateur.getBibliotheque() != null
               && utilisateur.getBibliotheque().isActif();
    } else if (utilisateur.getRole() == Role.LECTEUR) {
        return utilisateur.isActif(); 
    }
    return false;
}
}
