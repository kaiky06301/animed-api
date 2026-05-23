package br.com.fiap.clyvovet.service;

import br.com.fiap.clyvovet.dto.PetShopDTO;
import br.com.fiap.clyvovet.entity.PetShop;
import br.com.fiap.clyvovet.exception.BusinessException;
import br.com.fiap.clyvovet.exception.ResourceNotFoundException;
import br.com.fiap.clyvovet.repository.PetShopRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
public class PetShopService {

    private final PetShopRepository petShopRepository;

    @Transactional
    @CacheEvict(value = "petshops", allEntries = true)
    public PetShopDTO.Response criar(PetShopDTO.Request request) {
        if (petShopRepository.existsByCnpj(request.cnpj())) {
            throw new BusinessException("Já existe um pet shop com este CNPJ.");
        }

        PetShop petShop = PetShop.builder()
                .razaoSocial(request.razaoSocial())
                .nomeFantasia(request.nomeFantasia())
                .cnpj(request.cnpj())
                .endereco(request.endereco())
                .cidade(request.cidade())
                .uf(request.uf())
                .telefone(request.telefone())
                .taxaMensal(request.taxaMensal() != null ? request.taxaMensal() : new BigDecimal("99.90"))
                .comissaoPercentual(request.comissaoPercentual() != null ? request.comissaoPercentual() : new BigDecimal("5.00"))
                .ativo(true)
                .build();

        return toResponse(petShopRepository.save(petShop));
    }

    @Cacheable(value = "petshops", key = "#id")
    public PetShopDTO.Response buscarPorId(Long id) {
        return toResponse(buscarEntidade(id));
    }

    public PetShop buscarEntidade(Long id) {
        return petShopRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("PetShop", id));
    }

    public Page<PetShopDTO.Response> listar(Pageable pageable) {
        return petShopRepository.findAll(pageable).map(this::toResponse);
    }

    public Page<PetShopDTO.Response> listarAtivos(Pageable pageable) {
        return petShopRepository.findByAtivoTrue(pageable).map(this::toResponse);
    }

    public Page<PetShopDTO.Response> buscarPorCidadeUf(String cidade, String uf, Pageable pageable) {
        return petShopRepository.findByCidadeIgnoreCaseAndUfIgnoreCase(cidade, uf, pageable).map(this::toResponse);
    }

    public Page<PetShopDTO.Response> buscarPorNome(String nome, Pageable pageable) {
        return petShopRepository.findByNomeFantasiaContainingIgnoreCase(nome, pageable).map(this::toResponse);
    }

    @Transactional
    @CacheEvict(value = "petshops", allEntries = true)
    public PetShopDTO.Response atualizar(Long id, PetShopDTO.Request request) {
        PetShop petShop = buscarEntidade(id);

        petShop.setRazaoSocial(request.razaoSocial());
        petShop.setNomeFantasia(request.nomeFantasia());
        petShop.setEndereco(request.endereco());
        petShop.setCidade(request.cidade());
        petShop.setUf(request.uf());
        petShop.setTelefone(request.telefone());
        if (request.taxaMensal() != null) petShop.setTaxaMensal(request.taxaMensal());
        if (request.comissaoPercentual() != null) petShop.setComissaoPercentual(request.comissaoPercentual());

        return toResponse(petShopRepository.save(petShop));
    }

    @Transactional
    @CacheEvict(value = "petshops", allEntries = true)
    public void deletar(Long id) {
        if (!petShopRepository.existsById(id)) {
            throw new ResourceNotFoundException("PetShop", id);
        }
        petShopRepository.deleteById(id);
    }

    private PetShopDTO.Response toResponse(PetShop p) {
        return new PetShopDTO.Response(
                p.getId(),
                p.getRazaoSocial(),
                p.getNomeFantasia(),
                p.getCnpj(),
                p.getEndereco(),
                p.getCidade(),
                p.getUf(),
                p.getTelefone(),
                p.getTaxaMensal(),
                p.getComissaoPercentual(),
                p.getAtivo(),
                p.getDataParceria()
        );
    }
}
