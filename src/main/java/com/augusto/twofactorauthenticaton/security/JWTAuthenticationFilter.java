package com.augusto.twofactorauthenticaton.security;

import com.augusto.twofactorauthenticaton.user.User;
import com.augusto.twofactorauthenticaton.user.UserMapper;
import com.augusto.twofactorauthenticaton.user.UserService;
import com.auth0.jwt.JWT;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.web.authentication.AbstractAuthenticationProcessingFilter;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import static com.augusto.twofactorauthenticaton.security.SecurityConstants.*;
import static com.auth0.jwt.algorithms.Algorithm.HMAC512;
import static java.util.Objects.nonNull;
import static org.apache.commons.lang3.StringUtils.isEmpty;

public class JWTAuthenticationFilter extends AbstractAuthenticationProcessingFilter {

    private AuthenticationProvider authenticationProvider;
    private UserService userService;
    private UserMapper userMapper;
    private User creds;

    public JWTAuthenticationFilter(String url, AuthenticationProvider authenticationProvider, UserService userService, UserMapper userMapper) {
        super(new AntPathRequestMatcher(url));
        this.authenticationProvider = authenticationProvider;
        this.userService = userService;
        this.userMapper = userMapper;
    }

    @Override
    public Authentication attemptAuthentication(HttpServletRequest req, HttpServletResponse res) throws AuthenticationException {
        try {
            creds = new ObjectMapper().readValue(req.getInputStream(), User.class);
            Authentication authentication = authenticationProvider.authenticate(
                    new UsernamePasswordAuthenticationToken(creds.getUsername(), creds.getPassword(), creds.getAuthorities())
            );

            userService.validateOTP(res, authentication, creds);

            return authentication;
        } catch (Exception e) {
            try {
                unsuccessfulAuthentication(req, res, null);
            } catch (IOException ex) {
                ex.printStackTrace();
            }
            return null;
        }
    }

    @Override
    protected void successfulAuthentication(HttpServletRequest req,
                                            HttpServletResponse res,
                                            FilterChain chain,
                                            Authentication auth) throws IOException, ServletException {

        Object user;
        User userPrincipal = (User) (auth.getPrincipal());

        user = userMapper.toDTO(userPrincipal);

        String token = JWT.create()
                .withSubject(nonNull(auth.getName()) ? auth.getName() : userPrincipal.getUsername())
                .withArrayClaim("roles", auth.getAuthorities()
                        .stream()
                        .map(o -> ((GrantedAuthority) o).getAuthority())
                        .toArray(String[]::new))
                .withExpiresAt(new Date(System.currentTimeMillis() + EXPIRATION_TIME))
                .sign(HMAC512(SECRET.getBytes()));
        res.addHeader(HEADER_STRING, TOKEN_PREFIX + token);
        res.setContentType("application/json");


        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        String response = mapper.writeValueAsString(user);
        res.getWriter().write(response);

    }

    @Override
    protected void unsuccessfulAuthentication(HttpServletRequest request, HttpServletResponse response, AuthenticationException failed) throws IOException {
        response.setContentType("application/json");
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        response.setStatus(HttpStatus.UNAUTHORIZED.value());
        Map<String, Object> data = new HashMap<>();
        data.put("message", "Incorrect user or password");
        data.put("errors", Arrays.asList("Incorrect user or password"));
        response.getWriter().write(mapper.writeValueAsString(data));
    }
}
