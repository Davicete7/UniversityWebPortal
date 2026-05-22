package pl.dmcs.david.finalproject.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * Filter that intercepts every incoming HTTP request.
 * It checks for a valid JWT token in the "Authorization" header.
 */
public class AuthTokenFilter extends OncePerRequestFilter {

    @Autowired
    private JwtUtils jwtUtils;

    @Autowired
    private UserDetailsServiceImpl userDetailsService;

    /**
     * The core method that runs exactly once per request.
     */
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        try {
            // 1. Extract the token from the request header
            String jwt = parseJwt(request);

            // 2. If the token exists and is mathematically valid
            if (jwt != null && jwtUtils.validateJwtToken(jwt)) {

                // 3. Extract the username from the token
                String username = jwtUtils.getUserNameFromJwtToken(jwt);

                // 4. Load the user details from the database
                UserDetails userDetails = userDetailsService.loadUserByUsername(username);

                // 5. Create an Authentication object (the "stamped passport")
                UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(
                                userDetails,
                                null,
                                userDetails.getAuthorities());

                authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                // 6. Save the authentication securely in the Spring Context
                SecurityContextHolder.getContext().setAuthentication(authentication);
            }
        } catch (Exception e) {
            System.err.println("Cannot set user authentication: " + e.getMessage());
        }

        // Continue the filter chain (let the request pass to the Controller)
        filterChain.doFilter(request, response);
    }

    /**
     * Helper method to extract the JWT string from the HTTP Header.
     * Standard tokens come prefixed with "Bearer ".
     */
    private String parseJwt(HttpServletRequest request) {
        String headerAuth = request.getHeader("Authorization");

        if (StringUtils.hasText(headerAuth) && headerAuth.startsWith("Bearer ")) {
            return headerAuth.substring(7); // Remove "Bearer " prefix
        }

        return null;
    }
}
