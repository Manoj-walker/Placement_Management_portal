package edu.nit.placement.security;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import edu.nit.placement.dao.UserDAO;
import edu.nit.placement.model.user.User;
import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class JwtFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final UserDAO userDAO;

    public JwtFilter(JwtService jwtService, UserDAO userDAO) {
        this.jwtService = jwtService;
        this.userDAO = userDAO;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {
        String header = request.getHeader("Authorization");
        if (header != null && header.startsWith("Bearer ")) {
            try {
                Claims claims = jwtService.parse(header.substring(7));
                Long userId = Long.valueOf(claims.getSubject());
                Optional<User> user = userDAO.findById(userId);
                if (user.isPresent() && !user.get().isBlocked()) {
                    var authorities = List.of(
                            new SimpleGrantedAuthority("ROLE_" + user.get().getRole().name()));
                    var auth = new UsernamePasswordAuthenticationToken(
                            user.get(), null, authorities);
                    SecurityContextHolder.getContext().setAuthentication(auth);
                }
            } catch (Exception ignored) {
                // Invalid/expired token — request continues unauthenticated.
            }
        }
        filterChain.doFilter(request, response);
    }
}
