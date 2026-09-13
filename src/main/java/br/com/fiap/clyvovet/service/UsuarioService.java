package br.com.fiap.clyvovet.service;

import br.com.fiap.clyvovet.dto.UsuarioDTO;
import br.com.fiap.clyvovet.entity.Usuario;
import br.com.fiap.clyvovet.enums.Role;
import br.com.fiap.clyvovet.exception.BusinessException;
import br.com.fiap.clyvovet.exception.ResourceNotFoundException;
import br.com.fiap.clyvovet.repository.PetRepository;
import br.com.fiap.clyvovet.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;

/**
 * Administração das contas de acesso.
 *
 * Quem cuida disso é a clínica: ela abre o acesso de um tutor novo e, quando
 * alguém deixa a equipe, fecha o dele. Contas não são apagadas — o histórico
 * clínico que a pessoa produziu continua valendo, e apagar o usuário
 * arrancaria a autoria de registros que precisam ter dono.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final PetRepository petRepository;

    /** Todas as contas, com as ativas primeiro e os veterinários antes dos tutores. */
    public List<UsuarioDTO.Response> listar() {
        return usuarioRepository.findAll().stream()
                .sorted(Comparator
                        .comparing((Usuario u) -> !u.isEnabled())
                        .thenComparing(u -> u.getRole() != Role.DOUTOR)
                        .thenComparing(Usuario::getNome, Comparator.nullsLast(String::compareTo)))
                .map(this::toResponse)
                .toList();
    }

    /**
     * Liga ou desliga o acesso de uma conta.
     *
     * Duas travas que existem porque o erro aqui é caro: ninguém desliga a
     * própria conta — ficaria trancado para fora do sistema que estava
     * administrando — e a clínica não pode ficar sem nenhum veterinário ativo,
     * porque atos clínicos dependem desse perfil.
     */
    @Transactional
    public UsuarioDTO.Response mudarAcesso(Long id, boolean ativo) {
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Usuário", id));

        if (!ativo) {
            exigirQueNaoSejaEleMesmo(usuario);
            exigirOutroVeterinarioAtivo(usuario);
        }

        usuario.setAtivo(ativo);
        return toResponse(usuarioRepository.save(usuario));
    }

    private void exigirQueNaoSejaEleMesmo(Usuario alvo) {
        Authentication autenticacao = SecurityContextHolder.getContext().getAuthentication();

        if (autenticacao != null && alvo.getEmail().equals(autenticacao.getName())) {
            throw new BusinessException(
                    "Você não pode desativar o próprio acesso");
        }
    }

    private void exigirOutroVeterinarioAtivo(Usuario alvo) {
        if (alvo.getRole() != Role.DOUTOR) {
            return;
        }

        long veterinariosAtivos = usuarioRepository.findAll().stream()
                .filter(u -> u.getRole() == Role.DOUTOR && u.isEnabled())
                .count();

        if (veterinariosAtivos <= 1) {
            throw new BusinessException(
                    "A clínica precisa de ao menos um veterinário ativo");
        }
    }

    private UsuarioDTO.Response toResponse(Usuario usuario) {
        int pets = usuario.getTutor() == null
                ? 0
                : (int) petRepository.countByTutorId(usuario.getTutor().getId());

        return new UsuarioDTO.Response(
                usuario.getId(),
                usuario.getNome(),
                usuario.getEmail(),
                usuario.getRole(),
                usuario.isEnabled(),
                usuario.getDataCadastro(),
                usuario.getTutor() == null ? null : usuario.getTutor().getId(),
                pets
        );
    }
}
