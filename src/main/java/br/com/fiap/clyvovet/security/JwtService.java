package br.com.fiap.clyvovet.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.Map;

/**
 * Emissão e validação dos tokens JWT usados pelo aplicativo mobile.
 */
@Service
public class JwtService {

    private final SecretKey chave;
    private final long validadeMs;

    public JwtService(
            @Value("${animed.jwt.secret}") String segredo,
            @Value("${animed.jwt.validade-ms}") long validadeMs) {
        this.chave = Keys.hmacShaKeyFor(Decoders.BASE64.decode(segredo));
        this.validadeMs = validadeMs;
    }

    /** Gera o token com o e-mail no subject e o perfil como claim. */
    public String gerarToken(UserDetails usuario, Map<String, Object> claimsExtras) {
        Date agora = new Date();
        return Jwts.builder()
                .claims(claimsExtras)
                .subject(usuario.getUsername())
                .issuedAt(agora)
                .expiration(new Date(agora.getTime() + validadeMs))
                .signWith(chave)
                .compact();
    }

    public String extrairEmail(String token) {
        return extrairClaims(token).getSubject();
    }

    /** O token é válido se pertence ao usuário informado e ainda não expirou. */
    public boolean tokenValido(String token, UserDetails usuario) {
        return extrairEmail(token).equals(usuario.getUsername()) && !expirado(token);
    }

    public long getValidadeMs() {
        return validadeMs;
    }

    private boolean expirado(String token) {
        return extrairClaims(token).getExpiration().before(new Date());
    }

    private Claims extrairClaims(String token) {
        return Jwts.parser()
                .verifyWith(chave)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}
