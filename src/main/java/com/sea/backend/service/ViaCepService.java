package com.sea.backend.service;

import com.sea.backend.config.CacheConfig;
import com.sea.backend.dto.EnderecoResponseDTO;
import com.sea.backend.dto.ViaCepResponseDTO;
import com.sea.backend.exception.CepNaoEncontradoException;
import com.sea.backend.exception.ViaCepIndisponivelException;
import com.sea.backend.utils.DigitExtractor;
import com.sea.backend.utils.MaskUtils;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

@Service
public class ViaCepService {

    private static final String VIA_CEP_URL = "https://viacep.com.br/ws/{cep}/json/";

    private final RestTemplate restTemplate;

    public ViaCepService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    @Cacheable(value = CacheConfig.CACHE_ENDERECOS_CEP,
            key = "T(com.sea.backend.utils.DigitExtractor).onlyDigits(#cep)")
    public EnderecoResponseDTO consultar(String cep) {
        String cepDigits = DigitExtractor.onlyDigits(cep);
        ViaCepResponseDTO resposta = buscarNaViaCep(cepDigits);

        if (resposta == null || resposta.isErro()) {
            throw new CepNaoEncontradoException(cepDigits);
        }

        return new EnderecoResponseDTO(
                MaskUtils.maskCep(cepDigits),
                resposta.getLogradouro(),
                resposta.getBairro(),
                resposta.getLocalidade(),
                resposta.getUf(),
                null
        );
    }

    private ViaCepResponseDTO buscarNaViaCep(String cepDigits) {
        try {
            return restTemplate.getForObject(VIA_CEP_URL, ViaCepResponseDTO.class, cepDigits);
        } catch (RestClientException ex) {
            throw new ViaCepIndisponivelException(ex);
        }
    }
}
