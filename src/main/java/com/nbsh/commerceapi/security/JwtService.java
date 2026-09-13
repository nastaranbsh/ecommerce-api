package com.nbsh.commerceapi.security;

import com.nbsh.commerceapi.user.User;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;

@Service
public class JwtService {

    private final JwtEncoder jwtEncoder;
    private final String issuer;
    private final long accessTokenTtlSeconds;

    public JwtService(
            JwtEncoder jwtEncoder,
            @Value("${security.jwt.issuer}")
            String issuer,
            @Value("${security.jwt.access-token-ttl-seconds}")
            long accessTokenTtlSeconds
    ) {
        this.jwtEncoder = jwtEncoder;
        this.issuer = issuer;
        this.accessTokenTtlSeconds = accessTokenTtlSeconds;
    }

    public TokenResult generateAccessToken(
            User user
    ) {

        Instant now = Instant.now();

        Instant expiresAt =
                now.plusSeconds(
                        accessTokenTtlSeconds
                );

        JwtClaimsSet claims =
                JwtClaimsSet.builder()
                        .issuer(issuer)
                        .subject(user.getEmail())
                        .issuedAt(now)
                        .expiresAt(expiresAt)
                        .claim(
                                "userId",
                                user.getId()
                        )
                        .claim(
                                "roles",
                                List.of(
                                        user.getRole().name()
                                )
                        )
                        .build();

        JwsHeader header =
                JwsHeader
                        .with(MacAlgorithm.HS256)
                        .build();

        String token =
                jwtEncoder
                        .encode(
                                JwtEncoderParameters.from(
                                        header,
                                        claims
                                )
                        )
                        .getTokenValue();

        return new TokenResult(
                token,
                accessTokenTtlSeconds
        );
    }

    public record TokenResult(
            String token,
            long expiresIn
    ) {
    }
}