package com.sea.backend.config;

import com.sea.backend.entity.Role;
import com.sea.backend.entity.Usuario;
import com.sea.backend.repository.UsuarioRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class UsuarioSeeder implements CommandLineRunner {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    public UsuarioSeeder(UsuarioRepository usuarioRepository, PasswordEncoder passwordEncoder) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        seedIfAbsent("admin", "123qwe!@#", Role.ADMIN);
        seedIfAbsent("user", "123qwe123", Role.USER);
    }

    private void seedIfAbsent(String login, String senha, Role role) {
        if (usuarioRepository.findByLogin(login).isPresent()) {
            return;
        }
        Usuario usuario = new Usuario(login, passwordEncoder.encode(senha), role);
        usuarioRepository.save(usuario);
    }
}
