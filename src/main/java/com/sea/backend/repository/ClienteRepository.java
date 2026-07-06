package com.sea.backend.repository;

import com.sea.backend.entity.Cliente;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ClienteRepository extends JpaRepository<Cliente, Long> {

    boolean existsByCpf(String cpf);

    boolean existsByCpfAndIdNot(String cpf, Long id);

    Page<Cliente> findByNomeContainingIgnoreCaseAndCpfContaining(String nome, String cpf, Pageable pageable);
}
