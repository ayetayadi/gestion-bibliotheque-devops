package com.bibliotheque.gestion_bibliotheque.security;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.bibliotheque.gestion_bibliotheque.dao.UtilisateurRepository;
import com.bibliotheque.gestion_bibliotheque.entities.user.Utilisateur; // ✅ IMPORT
import com.bibliotheque.gestion_bibliotheque.security.UserDetailsImpl; // ✅ IMPORT

@Service
public class UserDetailsServiceImpl implements UserDetailsService {

    @Autowired
    private UtilisateurRepository utilisateurRepository;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {

        Optional<Utilisateur> utilisateurOpt =
                utilisateurRepository.findByEmail(email);

        Utilisateur utilisateur = utilisateurOpt.orElseThrow(
                () -> new UsernameNotFoundException("Utilisateur non trouvé : " + email)
        );

        return new UserDetailsImpl(utilisateur);
    }
}
