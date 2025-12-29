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
            .csrf(csrf -> csrf.disable())

            .authorizeHttpRequests(auth -> auth
                // PUBLIC
                .requestMatchers("/", "/login", "/register",
                        "/css/**", "/js/**", "/images/**").permitAll()

                // WebSocket + SockJS + STOMP
                .requestMatchers("/ws/**", "/topic/**", "/app/**", "/user/**").permitAll()

                // API CALLS
                .requestMatchers("/api/**").permitAll()

                // LECTEUR
                .requestMatchers("/catalogue/**", "/lecteur/**").hasRole("LECTEUR")

                // BIBLIOTHECAIRE
                .requestMatchers("/bibliothecaire/**").hasRole("BIBLIOTHECAIRE")

                // ADMIN
                .requestMatchers("/admin/**").hasRole("ADMIN")

                // SUPER ADMIN
                .requestMatchers("/super-admin/**").hasRole("SUPER_ADMIN")

                .anyRequest().authenticated()
            )

            .formLogin(form -> form
                .loginPage("/login")
                .loginProcessingUrl("/login")
                .usernameParameter("email")
                .passwordParameter("password")
                .defaultSuccessUrl("/dashboard", true)
                .failureUrl("/login?error=true")
                .permitAll()
            )

            .logout(logout -> logout
                .logoutUrl("/logout")
                .logoutSuccessUrl("/login?logout=true")
                .permitAll()
            );

        http.headers(headers -> headers.frameOptions(frame -> frame.disable()));

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
