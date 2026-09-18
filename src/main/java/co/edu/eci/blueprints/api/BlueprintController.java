package co.edu.eci.blueprints.api;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/blueprints")
@Tag(name = "Sample Blueprints", description = "Endpoints de plantilla de blueprints")
@SecurityRequirement(name = "bearerAuth")
public class BlueprintController {

    @GetMapping
    @PreAuthorize("hasAuthority('SCOPE_blueprints.read')")
    @Operation(summary = "Listar blueprints de ejemplo", description = "Retorna blueprints estáticos de plantilla")
    public List<Map<String, String>> list() {
        return List.of(
            Map.of("id", "b1", "name", "Casa de campo"),
            Map.of("id", "b2", "name", "Edificio urbano")
        );
    }

    @PostMapping
    @PreAuthorize("hasAuthority('SCOPE_blueprints.write')")
    @Operation(summary = "Crear blueprint de ejemplo", description = "Endpoint didáctico para prueba de creación")
    public Map<String, String> create(@RequestBody Map<String, String> in) {
        return Map.of("id", "new", "name", in.getOrDefault("name", "nuevo"));
    }
}
