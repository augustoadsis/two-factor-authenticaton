package com.augusto.twofactorauthenticaton.security;

import com.augusto.twofactorauthenticaton.user.UserMapper;
import com.augusto.twofactorauthenticaton.user.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import static java.util.Arrays.asList;

@EnableWebSecurity
public class WebSecurity extends WebSecurityConfigurerAdapter {

    @Autowired
    @Lazy
    UserService userService;
    @Autowired
    @Lazy
    UserMapper userMapper;

    @Autowired
    private UserAuthenticationProvider userAuthenticationProvider;

    protected String[] allowedEndpoints() {
        String[] allowedEndpoints = asList(
                "/login",
                "/users/password/forget/**",
                "/users/password/redefine/token/**",
                "/users/refreshToken",
                "/health/**"
        ).stream().toArray(String[]::new);
        return allowedEndpoints;
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http
                .cors().and()
                    .csrf().disable()
                    .authorizeRequests()
                    .antMatchers(allowedEndpoints()).permitAll()
                .anyRequest().authenticated()
                .and()
                    .addFilterAt(new JWTAuthenticationFilter("/login", userAuthenticationProvider, userService, userMapper), UsernamePasswordAuthenticationFilter.class)
                    .addFilter(new JWTAuthorizationFilter(authenticationManager()))
                .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS);
    }

    @Bean
    public BCryptPasswordEncoder bCryptPasswordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authManager() throws Exception {
        return this.authenticationManager();
    }

    @Bean
    CorsConfigurationSource corsConfigurationSource() {
        final UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();

        CorsConfiguration config = new CorsConfiguration();
        config.addExposedHeader("Authorization");
        config.applyPermitDefaultValues();
        config.addAllowedOrigin("*");
        config.addAllowedOrigin("/**");
        config.addExposedHeader("location");
        config.setAllowedMethods(asList("POST", "OPTIONS", "GET", "PATCH", "DELETE"));
        config.addAllowedMethod("*");

        source.registerCorsConfiguration("/**", config);
        return source;
    }

}
