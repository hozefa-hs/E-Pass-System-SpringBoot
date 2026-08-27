package com.porfolio.EPassSystemSpringboot.filters;

import com.porfolio.EPassSystemSpringboot.services.implementations.CustomUserDetailsService;
import com.porfolio.EPassSystemSpringboot.utils.JwtUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class JwtAuthFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    private final CustomUserDetailsService customUserDetailsService;


    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {

        //TODO - extract token from request
        String authHeader = request.getHeader("Authorization");

        String token = null;
        String usernameFromToken = null;

        if(authHeader != null && authHeader.startsWith("Bearer")) {
            token = authHeader.substring(7);
            usernameFromToken = jwtUtil.extractUsernameFromToken(token);
        }

        if(usernameFromToken != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            //TODO - fetch user from db and validate it with token's username
            UserDetails userDetails = customUserDetailsService.loadUserByUsername(usernameFromToken);

            //TODO - validate token
            if (jwtUtil.validateToken(token, usernameFromToken, userDetails)) {

                //TODO - create auth token with security details
                UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());

                //Not necessary but to set extra details to our security context
                //Do this if you want to set all request related details to context
                //so your context will have more information about this token and principal obejct
                authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                //TODO - set auth token to spring security context holder
                SecurityContextHolder.getContext().setAuthentication(authToken);
            }
        }

        //TODO - calls the next filter in the filter chain
        filterChain.doFilter(request, response);
    }
}
