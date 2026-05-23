package br.com.fiap.clyvovet.repository;

import br.com.fiap.clyvovet.entity.Pet;
import br.com.fiap.clyvovet.enums.Especie;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface PetRepository extends JpaRepository<Pet, Long> {

    Page<Pet> findByTutorId(Long idTutor, Pageable pageable);

    Page<Pet> findByEspecie(Especie especie, Pageable pageable);

    Page<Pet> findByNomeContainingIgnoreCase(String nome, Pageable pageable);

    @Query("SELECT p FROM Pet p WHERE p.tutor.id = :idTutor AND p.especie = :especie")
    Page<Pet> findByTutorAndEspecie(@Param("idTutor") Long idTutor,
                                    @Param("especie") Especie especie,
                                    Pageable pageable);
}
