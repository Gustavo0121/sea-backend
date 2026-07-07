package com.sea.backend.config;

import com.sea.backend.entity.Role;
import com.sea.backend.entity.Usuario;
import com.sea.backend.repository.UsuarioRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
class UsuarioSeederTest {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Test
    void deveCriarUsuarioAdminComSenhaCriptografada() {
        Usuario admin = usuarioRepository.findByLogin("admin").orElseThrow();

        assertThat(admin.getId()).isNotNull();
        assertThat(admin.getRole()).isEqualTo(Role.ADMIN);
        assertThat(admin.getSenha()).isNotEqualTo("123qwe!@#");
        assertThat(passwordEncoder.matches("123qwe!@#", admin.getSenha())).isTrue();
    }

    @Test
    void deveCriarUsuarioPadraoComSenhaCriptografada() {
        Usuario user = usuarioRepository.findByLogin("user").orElseThrow();

        assertThat(user.getId()).isNotNull();
        assertThat(user.getRole()).isEqualTo(Role.USER);
        assertThat(user.getSenha()).isNotEqualTo("123qwe123");
        assertThat(passwordEncoder.matches("123qwe123", user.getSenha())).isTrue();
    }
}
