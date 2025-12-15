package com.bibliotheque.gestion_bibliotheque.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableMethodSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {

        http
            // ❗ Désactivation CSRF car tu n'utilises pas de token
            .csrf(csrf -> csrf.disable())

            .authorizeHttpRequests(auth -> auth

                // 🌍 PUBLIC
                .requestMatchers(
                        "/",
                        "/login",
                        "/register",
                        "/css/**",
                        "/js/**",
                        "/images/**"
                ).permitAll()

                // 📚 LECTEUR — accès GET + POST
                .requestMatchers("/catalogue/**").hasRole("LECTEUR")
                .requestMatchers("/lecteur/**").hasRole("LECTEUR")

                // 👨‍🏫 BIBLIOTHÉCAIRE
                .requestMatchers("/bibliothecaire/**").hasRole("BIBLIOTHECAIRE")

                // 🧑‍💼 ADMIN
                .requestMatchers("/admin/**").hasRole("ADMIN")

                // 👑 SUPER ADMIN
                .requestMatchers("/super-admin/**").hasRole("SUPER_ADMIN")

                // 🔐 Tout le reste nécessite une authentification
                .anyRequest().authenticated()
            )

            // 🔑 FORM LOGIN
            .formLogin(form -> form
                    .loginPage("/login")
                    .loginProcessingUrl("/login") // route POST
                    .usernameParameter("email")
                    .passwordParameter("password")
                    .defaultSuccessUrl("/dashboard", true)
                    .failureUrl("/login?error=true")
                    .permitAll()
            )

            // 🚪 LOGOUT
            .logout(logout -> logout
                    .logoutUrl("/logout")
                    .logoutSuccessUrl("/login?logout=true")
                    .permitAll()
            );

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
