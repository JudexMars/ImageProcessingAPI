package org.judexmars.imagecrud.security;

import io.micrometer.common.util.StringUtils;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.judexmars.imagecrud.service.JwtTokenService;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * Request filter for checking JWT in the Authorization header.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class JwtRequestFilter extends OncePerRequestFilter {

  private final JwtTokenService jwtTokenService;
  public static final String BEARER_PREFIX = "Bearer ";
  public static final String HEADER_NAME = "Authorization";

  @Override
  protected void doFilterInternal(HttpServletRequest request,
                                  @NotNull HttpServletResponse response,
                                  @NotNull FilterChain filterChain
  ) throws ServletException, IOException {
    var authHeader = request.getHeader(HEADER_NAME);
    log.info("AuthHeader: {}", authHeader);
    if (StringUtils.isEmpty(authHeader) || !authHeader.startsWith(BEARER_PREFIX)) {
      filterChain.doFilter(request, response);
      return;
    }

    var jwt = authHeader.substring(BEARER_PREFIX.length());
    var username = jwtTokenService.getUsernameFromAccessToken(jwt);
    if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
      var token = new UsernamePasswordAuthenticationToken(
          username,
          null,
          jwtTokenService
              .getRolesFromAccessToken(jwt)
              .stream()
              .map(SimpleGrantedAuthority::new)
              .toList()
      );
      var context = SecurityContextHolder.createEmptyContext();
      token.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
      context.setAuthentication(token);
      SecurityContextHolder.setContext(context);
      log.info("Security context is set: {}", context);
    }

    filterChain.doFilter(request, response);
  }
}
