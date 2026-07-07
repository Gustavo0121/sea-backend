package com.sea.backend.service;

import com.sea.backend.dto.LoginRequest;
import com.sea.backend.dto.LoginResponse;
import com.sea.backend.entity.Usuario;
import com.sea.backend.repository.UsuarioRepository;
import com.sea.backend.security.JwtTokenProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.stereotype.Service;

/**
 * Only the login (username) is ever logged here — request.getSenha() and the generated
 * JWT are never passed to the logger, per the "nunca registrar senha/token" requirement.
 */
@Service
public class AuthService {

    private static final String BEARER = "Bearer";
    private static final Logger log = LoggerFactory.getLogger(AuthService.class);

    private final AuthenticationManager authenticationManager;
    private final UsuarioRepository usuarioRepository;
    private final JwtTokenProvider jwtTokenProvider;

    public AuthService(AuthenticationManager authenticationManager,
                        UsuarioRepository usuarioRepository,
                        JwtTokenProvider jwtTokenProvider) {
        this.authenticationManager = authenticationManager;
        this.usuarioRepository = usuarioRepository;
        this.jwtTokenProvider = jwtTokenProvider;
    }

    public LoginResponse autenticar(LoginRequest request) {
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getLogin(), request.getSenha()));
        } catch (AuthenticationException ex) {
            log.warn("Falha de login para o usuário '{}'.", request.getLogin());
            throw ex;
        }

        Usuario usuario = usuarioRepository.findByLogin(request.getLogin())
                .orElseThrow(() -> new BadCredentialsException("Credenciais inválidas."));

        String token = jwtTokenProvider.generateToken(usuario.getLogin(), usuario.getRole());
        log.info("Login bem-sucedido para o usuário '{}'.", usuario.getLogin());
        return new LoginResponse(token, BEARER, jwtTokenProvider.getExpirationSeconds());
    }
}
