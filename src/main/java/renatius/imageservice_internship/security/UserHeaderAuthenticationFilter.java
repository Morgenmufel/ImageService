package renatius.imageservice_internship.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import renatius.imageservice_internship.entities.SocialUser;
import renatius.imageservice_internship.repository.SocialUserRepository;
import java.io.IOException;
import java.util.Collections;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class UserHeaderAuthenticationFilter extends OncePerRequestFilter {

    @Value("${INTERNAL_GATEWAY_KEY}")
    private String internalGatewayKey;

    private final SocialUserRepository socialUserRepository;

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
                                    @NonNull HttpServletResponse response,
                                    @NonNull FilterChain filterChain) throws ServletException, IOException {

        String path = request.getRequestURI();
        if (path.matches("^/api/images/[^/]+/content$")) {filterChain.doFilter(request, response); return;}
        String internalKey = request.getHeader("X-Internal-Key");
        if (internalKey == null || !internalGatewayKey.equals(internalKey)) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json");
            response.getWriter().write("{\"error\":\"Access denied: invalid internal gateway key\"}");
            return;
        }
        String userIdHeader = request.getHeader("X-User-Id");
        String usernameHeader = request.getHeader("X-Username");
        if (userIdHeader != null && usernameHeader != null) {
            UUID userId = UUID.fromString(userIdHeader);
            String username = usernameHeader;
            if(!socialUserRepository.existsById(userId)) {
                socialUserRepository.save(SocialUser.builder()
                                .id(userId)
                                .username(username).build());
            }
            setCustomUserDetailsToSecurityContextHolder(userId, username, request);
        }
        filterChain.doFilter(request, response);
    }

    private void setCustomUserDetailsToSecurityContextHolder(UUID userId, String username, HttpServletRequest request) {
        CustomUserPrincipal principal = new CustomUserPrincipal(userId, username);
        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(
                        principal,
                        null,
                        Collections.emptyList()
                );
        authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }
}
