package br.com.fiap.clyvovet.repository;

import br.com.fiap.clyvovet.entity.PetShop;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PetShopRepository extends JpaRepository<PetShop, Long> {

    Optional<PetShop> findByCnpj(String cnpj);

    boolean existsByCnpj(String cnpj);

    Page<PetShop> findByAtivoTrue(Pageable pageable);

    Page<PetShop> findByCidadeIgnoreCaseAndUfIgnoreCase(String cidade, String uf, Pageable pageable);

    Page<PetShop> findByNomeFantasiaContainingIgnoreCase(String nome, Pageable pageable);
}
