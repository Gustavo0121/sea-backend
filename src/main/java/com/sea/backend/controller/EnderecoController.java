package com.sea.backend.controller;

import com.sea.backend.dto.EnderecoResponseDTO;
import com.sea.backend.service.ViaCepService;
import com.sea.backend.utils.CepFormato;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.constraints.Pattern;

@RestController
@RequestMapping("/enderecos")
@Validated
public class EnderecoController {

    private final ViaCepService viaCepService;

    public EnderecoController(ViaCepService viaCepService) {
        this.viaCepService = viaCepService;
    }

    @GetMapping("/{cep}")
    public ResponseEntity<EnderecoResponseDTO> consultarCep(
            @PathVariable
            @Pattern(regexp = CepFormato.REGEX, message = "CEP deve estar no formato 00000-000 ou 00000000.")
            String cep) {
        return ResponseEntity.ok(viaCepService.consultar(cep));
    }
}
