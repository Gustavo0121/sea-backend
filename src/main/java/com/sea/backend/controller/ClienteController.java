package com.sea.backend.controller;

import com.sea.backend.dto.ClienteRequestDTO;
import com.sea.backend.dto.ClienteResponseDTO;
import com.sea.backend.service.ClienteService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

@RestController
@RequestMapping("/clientes")
public class ClienteController {

    private final ClienteService clienteService;

    public ClienteController(ClienteService clienteService) {
        this.clienteService = clienteService;
    }

    @GetMapping
    public ResponseEntity<Page<ClienteResponseDTO>> listar(
            @RequestParam(required = false) String nome,
            @RequestParam(required = false) String cpf,
            @PageableDefault(size = 20, sort = "nome") Pageable pageable) {
        return ResponseEntity.ok(clienteService.listar(nome, cpf, pageable));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ClienteResponseDTO> buscarPorId(@PathVariable Long id) {
        return ResponseEntity.ok(clienteService.buscarPorId(id));
    }

    @PostMapping
    public ResponseEntity<ClienteResponseDTO> cadastrar(@Valid @RequestBody ClienteRequestDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(clienteService.cadastrar(dto));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ClienteResponseDTO> atualizar(@PathVariable Long id, @Valid @RequestBody ClienteRequestDTO dto) {
        return ResponseEntity.ok(clienteService.atualizar(id, dto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> excluir(@PathVariable Long id) {
        clienteService.excluir(id);
        return ResponseEntity.noContent().build();
    }
}
