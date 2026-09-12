package br.com.fiap.clyvovet.entity;

import br.com.fiap.clyvovet.enums.Role;
import jakarta.persistence.*;
import org.hibernate.type.NumericBooleanConverter;
import lombok.*;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

/**
 * Credencial de acesso à aplicação.
 * Um usuário do perfil TUTOR fica vinculado ao seu registro em TB_TUTOR;
 * o perfil DOUTOR não possui esse vínculo.
 */
@Entity
@Table(name = "TB_USUARIO", uniqueConstraints = {
        @UniqueConstraint(name = "UK_USUARIO_EMAIL", columnNames = "EMAIL")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Usuario implements UserDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID_USUARIO")
    private Long id;

    @Column(name = "NOME", length = 120)
    private String nome;

    @Column(name = "EMAIL", nullable = false, length = 150)
    private String email;

    /** Senha armazenada com hash BCrypt - nunca em texto puro. */
    @Column(name = "SENHA", nullable = false, length = 100)
    private String senha;

    @Enumerated(EnumType.STRING)
    @Column(name = "ROLE", nullable = false, length = 20)
    private Role role;

    /** Vínculo com o tutor correspondente (nulo quando o perfil é DOUTOR). */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ID_TUTOR", foreignKey = @ForeignKey(name = "FK_USUARIO_TUTOR"))
    private Tutor tutor;

    /**
     * Gravado como 0 ou 1, porque a coluna é NUMBER(1) no Oracle.
     *
     * Sem o conversor, o Hibernate compara a coluna com o literal booleano
     * e a consulta falha — foi o que quebrava o ranking de tutores.
     */
    @Column(name = "ATIVO", nullable = false)
    @Convert(converter = NumericBooleanConverter.class)
    @Builder.Default
    private Boolean ativo = true;

    @Column(name = "DATA_CADASTRO", nullable = false)
    @Builder.Default
    private LocalDateTime dataCadastro = LocalDateTime.now();

    // ---- contrato do Spring Security -------------------------------------

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_" + role.name()));
    }

    @Override
    public String getPassword() {
        return senha;
    }

    @Override
    public String getUsername() {
        return email;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return Boolean.TRUE.equals(ativo);
    }
}
