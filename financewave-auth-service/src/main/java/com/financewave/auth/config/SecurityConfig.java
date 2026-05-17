package com.financewave.auth.config;

import com.financewave.auth.security.JwtFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.*;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
public class SecurityConfig {

    @Autowired
    private JwtFilter jwtFilter;

   /* @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {

        http.csrf(csrf -> csrf.disable())
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/auth/**").permitAll()
                .requestMatchers("/admin/**").hasRole("ADMIN")
                //.requestMatchers("/user/**").hasAnyRole("USER", "ADMIN")
                //.requestMatchers("/employee/**").hasRole("EMPLOYEE")
                //.requestMatchers("/api/admin/**").hasRole("ADMIN")
                .requestMatchers("/api/user/**").hasAnyRole("USER", "ADMIN")
                .requestMatchers("/api/employee/**").hasRole("EMPLOYEE")
                .anyRequest().authenticated()
            )
            .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }*/
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {

        http
            .csrf(csrf -> csrf.disable())

            .authorizeHttpRequests(auth -> auth

                // ✅ PUBLIC APIs
                .requestMatchers(
                    "/auth/register",
                    "/auth/login",
                    "/auth/refresh",
                    "/auth/forgot-password",
                    "/auth/reset-password",
                    "/auth/send-otp",
                    "/auth/resend-otp",
                    "/auth/verify-otp"
                ).permitAll()

                // ✅ SELF LOGOUT
                .requestMatchers("/auth/logout").authenticated()
                .requestMatchers("/auth/logout-all").authenticated()

                // ✅ ADMIN CONTROL APIs
                .requestMatchers("/auth/admin/**").hasRole("ADMIN")

                // ✅ BUSINESS APIs
                .requestMatchers("/api/admin/**").hasRole("ADMIN")
                .requestMatchers("/api/user/**").hasAnyRole("USER", "ADMIN")
                .requestMatchers("/api/employee/**").hasRole("EMPLOYEE")

                // ✅ DEFAULT
                .anyRequest().authenticated()
            )

            .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public BCryptPasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
    
    
}