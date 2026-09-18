package co.edu.eci.blueprints.auth;

import co.edu.eci.blueprints.security.InMemoryUserService;
import co.edu.eci.blueprints.security.RsaKeyProperties;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.jwt.*;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.Map;

@RestController
@RequestMapping("/auth")
@Tag(name = "Authentication", description = "Endpoints de autenticación y emisión de tokens JWT")
public class AuthController {

    private final JwtEncoder encoder;
    private final InMemoryUserService userService;
    private final RsaKeyProperties props;

    public AuthController(JwtEncoder encoder, InMemoryUserService userService, RsaKeyProperties props) {
        this.encoder = encoder;
        this.userService = userService;
        this.props = props;
    }

    public record LoginRequest(
            @Schema(description = "Nombre de usuario registrado", example = "student") String username,
            @Schema(description = "Contraseña en texto plano", example = "student123") String password) {
    }

    public record TokenResponse(
            @Schema(description = "Token JWT firmado con RS256") String access_token,
            @Schema(description = "Tipo de token", example = "Bearer") String token_type,
            @Schema(description = "Tiempo de vida del token en segundos", example = "60") long expires_in) {
    }

    @PostMapping("/login")
    @SecurityRequirements
    @Operation(summary = "Inicio de sesión", description = "Autentica al usuario y emite un token JWT firmado con RS256")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Autenticación exitosa",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = TokenResponse.class))),
            @ApiResponse(responseCode = "401", description = "Credenciales inválidas")
    })
    public ResponseEntity<?> login(@RequestBody LoginRequest req) {
        if (!userService.isValid(req.username(), req.password())) {
            return ResponseEntity.status(401).body(Map.of("error", "invalid_credentials"));
        }

        Instant now = Instant.now();
        long ttl = props.tokenTtlSeconds() != null ? props.tokenTtlSeconds() : 3600;
        Instant exp = now.plusSeconds(ttl);

        String scope = switch (req.username()) {
            case "assistant" -> "blueprints.read";
            default -> "blueprints.read blueprints.write";
        };

        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer(props.issuer())
                .issuedAt(now)
                .expiresAt(exp)
                .subject(req.username())
                .claim("scope", scope)
                .build();

        JwsHeader jws = JwsHeader.with(() -> "RS256").build();
        String token = this.encoder.encode(JwtEncoderParameters.from(jws, claims)).getTokenValue();

        return ResponseEntity.ok(new TokenResponse(token, "Bearer", ttl));
    }
}
