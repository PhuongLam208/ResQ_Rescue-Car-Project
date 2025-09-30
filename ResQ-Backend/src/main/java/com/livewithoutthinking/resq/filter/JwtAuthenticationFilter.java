package com.livewithoutthinking.resq.filter;

import com.livewithoutthinking.resq.util.JwtUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    @Autowired
    private JwtUtil jwtUtil;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {

        System.out.println("Request : " +request);
        System.out.println("Response : " +response);
        System.out.println("Filterchain : " +filterChain);

        String token = request.getHeader("Authorization");
        System.out.println("[JwtAuthenticationFilter] Authorization header: " + token);

        if (token != null && token.startsWith("Bearer ")) {
            token = token.substring(7);
            System.out.println("[JwtAuthenticationFilter] Extracted token: " + token);
            try {
                String username = jwtUtil.extractUsername(token); // lấy username
                String role = jwtUtil.extractRole(token);
                String roleWithoutPrefix = role.split("_")[1];
                System.out.println("[JwtAuthenticationFilter] Extracted username: " + username);
                System.out.println("[JwtAuthenticationFilter] Extracted role: " + role);
                System.out.println("[JwtAuthenticationFilter] Extracted roleWithoutPrefix: " + roleWithoutPrefix);


                if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                    if (jwtUtil.validateToken(token, username)) {
                        var authorities = List.of(new SimpleGrantedAuthority(role));
                        System.out.println("[JwtAuthenticationFilter] Authorities set: " + authorities);

                        UsernamePasswordAuthenticationToken authentication =
                                new UsernamePasswordAuthenticationToken(username, null, authorities);

                        authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                        SecurityContextHolder.getContext().setAuthentication(authentication);

                        System.out.println("[JwtAuthenticationFilter] Authentication set in SecurityContextHolder");
                    } else {
                        System.out.println("[JwtAuthenticationFilter] Token validation failed");
                    }
                } else {
                    System.out.println("[JwtAuthenticationFilter] Username is null or Authentication already exists");
                }
            } catch (Exception e) {
                System.out.println("[JwtAuthenticationFilter] Exception parsing token: " + e.getMessage());
            }
        }else {
            System.out.println("[JwtAuthenticationFilter] No Bearer token found");
        }
        filterChain.doFilter(request, response);
    }
}
