package co.edu.eci.blueprints.controllers;

import co.edu.eci.blueprints.model.Blueprint;
import co.edu.eci.blueprints.model.Point;
import co.edu.eci.blueprints.persistence.BlueprintNotFoundException;
import co.edu.eci.blueprints.persistence.BlueprintPersistenceException;
import co.edu.eci.blueprints.services.BlueprintsServices;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.access.prepost.PreAuthorize;

import java.util.*;

@RestController
@RequestMapping("/api/v1/blueprints")
@Tag(name = "Blueprints", description = "Operations for managing blueprints")
public class BlueprintsAPIController {

    private final BlueprintsServices services;

    public BlueprintsAPIController(BlueprintsServices services) {
        this.services = services;
    }

    // GET /api/v1/blueprints
    @GetMapping
    @PreAuthorize("hasAuthority('SCOPE_blueprints.read')")
    @Operation(summary = "List all blueprints", description = "Returns every blueprint stored in the system")
    @io.swagger.v3.oas.annotations.responses.ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Blueprints retrieved")
    })
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<ApiResponse<Set<Blueprint>>> getAll() {
        return ResponseEntity.ok(response(HttpStatus.OK, "Blueprints retrieved", services.getAllBlueprints()));
    }

    // GET /api/v1/blueprints/{author}
    @GetMapping("/{author}")
    @PreAuthorize("hasAuthority('SCOPE_blueprints.read')")
    @Operation(summary = "List blueprints by author", description = "Returns all blueprints belonging to an author")
    @io.swagger.v3.oas.annotations.responses.ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Blueprints retrieved"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Author has no blueprints")
    })
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<ApiResponse<?>> byAuthor(@PathVariable String author) {
        try {
            return ResponseEntity.ok(response(HttpStatus.OK, "Blueprints retrieved",
                    services.getBlueprintsByAuthor(author)));
        } catch (BlueprintNotFoundException e) {
            return error(HttpStatus.NOT_FOUND, e.getMessage());
        }
    }

    // GET /api/v1/blueprints/{author}/{bpname}
    @GetMapping("/{author}/{bpname}")
    @PreAuthorize("hasAuthority('SCOPE_blueprints.read')")
    @Operation(summary = "Get a blueprint", description = "Returns one blueprint identified by author and name")
    @io.swagger.v3.oas.annotations.responses.ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Blueprint retrieved"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Blueprint not found")
    })
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<ApiResponse<?>> byAuthorAndName(@PathVariable String author, @PathVariable String bpname) {
        try {
            return ResponseEntity.ok(response(HttpStatus.OK, "Blueprint retrieved",
                    services.getBlueprint(author, bpname)));
        } catch (BlueprintNotFoundException e) {
            return error(HttpStatus.NOT_FOUND, e.getMessage());
        }
    }

    // POST /api/v1/blueprints
    @PostMapping
    @PreAuthorize("hasAuthority('SCOPE_blueprints.write')")
    @Operation(summary = "Create a blueprint", description = "Creates a blueprint with its initial points")
    @io.swagger.v3.oas.annotations.responses.ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "Blueprint created"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid request or duplicate blueprint")
    })
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<ApiResponse<?>> add(@Valid @RequestBody NewBlueprintRequest req) {
        try {
            Blueprint bp = new Blueprint(req.author(), req.name(), req.points());
            services.addNewBlueprint(bp);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(response(HttpStatus.CREATED, "Blueprint created", bp));
        } catch (BlueprintPersistenceException e) {
            return error(HttpStatus.BAD_REQUEST, e.getMessage());
        }
    }

    // PUT /api/v1/blueprints/{author}/{bpname}/points
    @PutMapping("/{author}/{bpname}/points")
    @PreAuthorize("hasAuthority('SCOPE_blueprints.write')")
    @Operation(summary = "Add a point", description = "Adds a point to an existing blueprint")
    @io.swagger.v3.oas.annotations.responses.ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "202", description = "Point accepted"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Blueprint not found"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid request body")
    })
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<ApiResponse<?>> addPoint(@PathVariable String author, @PathVariable String bpname,
            @Valid @RequestBody Point p) {
        try {
            services.addPoint(author, bpname, p.x(), p.y());
            return ResponseEntity.status(HttpStatus.ACCEPTED)
                    .body(response(HttpStatus.ACCEPTED, "Point accepted", p));
        } catch (BlueprintNotFoundException e) {
            return error(HttpStatus.NOT_FOUND, e.getMessage());
        }
    }

    @ExceptionHandler({ MethodArgumentNotValidException.class, HttpMessageNotReadableException.class })
    public ResponseEntity<ApiResponse<?>> handleBadRequest(Exception exception) {
        String message = exception instanceof MethodArgumentNotValidException validationException
                ? validationException.getBindingResult().getFieldErrors().stream()
                        .findFirst()
                        .map(error -> error.getField() + ": " + error.getDefaultMessage())
                        .orElse("Invalid request")
                : "Invalid request body";
        return error(HttpStatus.BAD_REQUEST, message);
    }

    private <T> ApiResponse<T> response(HttpStatus status, String message, T data) {
        return new ApiResponse<>(status.value(), message, data);
    }

    private ResponseEntity<ApiResponse<?>> error(HttpStatus status, String message) {
        return ResponseEntity.status(status).body(response(status, message, null));
    }

    public record NewBlueprintRequest(
            @NotBlank String author,
            @NotBlank String name,
            @NotNull @Valid java.util.List<Point> points) {
    }

    // PUT /api/v1/blueprints/{author}/{bpname}
    @PutMapping("/{author}/{bpname}")
    @PreAuthorize("hasAuthority('SCOPE_blueprints.write')")
    @Operation(summary = "Update a blueprint", description = "Updates the points of an existing blueprint")
    @io.swagger.v3.oas.annotations.responses.ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "202", description = "Blueprint updated"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Blueprint not found"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid request body")
    })
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<?> updatePoints(@PathVariable String author, @PathVariable String bpname,
            @RequestBody List<Point> points) {
        try {
            services.updateBlueprint(author, bpname, points);
            return ResponseEntity.status(HttpStatus.ACCEPTED)
                    .body(response(HttpStatus.ACCEPTED, "Blueprint updated", null));
        } catch (BlueprintNotFoundException e) {
            return error(HttpStatus.NOT_FOUND, e.getMessage());
        }
    }

}
