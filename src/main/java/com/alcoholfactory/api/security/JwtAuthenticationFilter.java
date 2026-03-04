package com.alcoholfactory.api.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.lang.NonNull;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * Filtr JWT – w kolejnym etapie będzie parsował nagłówek Authorization (Bearer token)
 * i ustawiał SecurityContext. Na razie tylko przekazuje request dalej.
 */
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {
        // TODO: odczytać Bearer token, zweryfikować JWT, ustawić Authentication w SecurityContext
        if (SecurityContextHolder.getContext().getAuthentication() == null) {
            // Brak tokena lub niepoprawny – request przechodzi dalej (np. do kontrolerów publicznych)
        }
        filterChain.doFilter(request, response);
    }
}
