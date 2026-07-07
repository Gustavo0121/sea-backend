package com.sea.backend.service;

import com.sea.backend.dto.LoginRequest;
import com.sea.backend.dto.LoginResponse;
import com.sea.backend.entity.Usuario;
import com.sea.backend.repository.UsuarioRepository;
import com.sea.backend.security.JwtTokenProvider;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    private static final String BEARER = "Bearer";

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
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getLogin(), request.getSenha()));

        Usuario usuario = usuarioRepository.findByLogin(request.getLogin())
                .orElseThrow(() -> new BadCredentialsException("Credenciais inválidas."));

        String token = jwtTokenProvider.generateToken(usuario.getLogin(), usuario.getRole());
        return new LoginResponse(token, BEARER, jwtTokenProvider.getExpirationSeconds());
    }
}
