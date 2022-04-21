package com.sbs.learning.config.filters;

import java.io.IOException;
import java.util.Optional;
import java.util.UUID;
import java.util.logging.Logger;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.sbs.learning.model.entity.User;
import com.sbs.learning.model.repository.UserRepository;
import com.sbs.learning.utils.jwt.JwtManager;
import com.sbs.learning.utils.jwt.Payload;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.security.SignatureException;

@Component
public class JwtInputFilter extends OncePerRequestFilter {

    private UserRepository userRepository;
    private final static Logger logger = Logger.getLogger(JwtInputFilter.class.getName());
    private final String AUTHORIZATION_HEADER = "Authorization";
    private final String BEARER_PREFIX = "Bearer";

    public JwtInputFilter(UserRepository ur) {
        this.userRepository = ur;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        logger.info("token verification started.");

        String header = request.getHeader(AUTHORIZATION_HEADER),
                username = null,
                uuid = null,
                authToken = null;

        Payload payload = null;
        if (header != null && header.startsWith(BEARER_PREFIX)) {
            authToken = header.replace(BEARER_PREFIX, "");
            try {
                JwtManager jm = new JwtManager();
                payload = jm.getJwt(authToken);

                username = payload.getUsername();
                uuid = payload.getUuid();

            } catch (IllegalArgumentException | ExpiredJwtException | SignatureException e) {
                if (e instanceof IllegalArgumentException || e instanceof SignatureException) {
                    logger.severe(e.getMessage());
                    return;
                }
                logger.info(e.getMessage());
            }
        } else {
            logger.warning("token is null");
        }

        SecurityContext context = SecurityContextHolder.getContext();

        Optional<User> userOptional = null;
        if (uuid != null) {
            userOptional = this.userRepository.findById(UUID.fromString(uuid));
        }

        if (username != null && context.getAuthentication() == null && userOptional.isPresent()) {

            this.setUserInContext(this.authenticate(username, userOptional.get().getPassword(), request));
            request.setAttribute("user", payload);
            logger.info("authenticated user " + username + ", setting security context");

        }
        logger.info("token verification finalized.");
        filterChain.doFilter(request, response);
    }

    private Authentication authenticate(String username, String password, HttpServletRequest request) {
        UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(username, password);
        authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
        return authentication;
    }

    /*
	 * Aqui de fato ocorre a autenticação do usuário.
	 * */
    private void setUserInContext(Authentication authentication) {
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }
}
