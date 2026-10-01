package co.edu.eci.blueprints.persistence;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

import co.edu.eci.blueprints.entity.BluePrintPoint;
import co.edu.eci.blueprints.entity.BluePrint;
import co.edu.eci.blueprints.model.Blueprint;
import co.edu.eci.blueprints.model.Point;

@Component
public class PostgressBluePrintPersistence implements BlueprintPersistence {

    private final SpringDataBlueprintRepository repository;

    public PostgressBluePrintPersistence(SpringDataBlueprintRepository repository) {
        this.repository = repository;
    }

    @Override
    public void saveBlueprint(Blueprint bp) throws BlueprintPersistenceException {
        if (repository.findByAuthorAndName(bp.getAuthor(), bp.getName()).isPresent()) {
            throw new BlueprintPersistenceException(
                    "Blueprint already exists: " + bp.getAuthor() + ":" + bp.getName());
        }

        BluePrint entity = new BluePrint(bp.getAuthor(), bp.getName());
        bp.getPoints().forEach(point -> entity.addPoint(point.x(), point.y()));
        repository.save(entity);
    }

    @Override
    public Blueprint getBlueprint(String author, String name) throws BlueprintNotFoundException {
        BluePrint entity = repository.findByAuthorAndName(author, name)
                .orElseThrow(() -> new BlueprintNotFoundException(
                        "Blueprint not found: %s/%s".formatted(author, name)));
        return toModel(entity);
    }

    @Override
    public Set<Blueprint> getBlueprintsByAuthor(String author) throws BlueprintNotFoundException {
        Set<Blueprint> blueprints = repository.findAll().stream()
                .filter(entity -> entity.getAuthor().equals(author))
                .map(this::toModel)
                .collect(Collectors.toSet());

        if (blueprints.isEmpty()) {
            throw new BlueprintNotFoundException("No blueprints for author: " + author);
        }
        return blueprints;
    }

    @Override
    public Set<Blueprint> getAllBlueprints() {
        return repository.findAll().stream()
                .map(this::toModel)
                .collect(Collectors.toCollection(HashSet::new));
    }

    @Override
    public void addPoint(String author, String name, int x, int y) throws BlueprintNotFoundException {
        BluePrint entity = repository.findByAuthorAndName(author, name)
                .orElseThrow(() -> new BlueprintNotFoundException(
                        "Blueprint not found: %s/%s".formatted(author, name)));
        entity.addPoint(x, y);
        repository.save(entity);
    }

    private Blueprint toModel(BluePrint entity) {
        List<Point> points = entity.getPoints().stream()
                .map(this::toModel)
                .toList();
        return new Blueprint(entity.getAuthor(), entity.getName(), points);
    }

    private Point toModel(BluePrintPoint point) {
        return new Point(point.getX(), point.getY());
    }

    @Override
    public void updateBlueprint(String author, String name, List<Point> points) throws BlueprintNotFoundException {
        BluePrint entity = repository.findByAuthorAndName(author, name)
                .orElseThrow(() -> new BlueprintNotFoundException(
                        "Blueprint not found: %s/%s".formatted(author, name)));
        entity.getPoints().clear();
        points.forEach(p -> entity.addPoint(p.x(), p.y()));
        repository.save(entity);
    }

    @Override
    public void deleteBlueprint(String author, String name) throws BlueprintNotFoundException {
        BluePrint entity = repository.findByAuthorAndName(author, name)
                .orElseThrow(() -> new BlueprintNotFoundException(
                        "Blueprint not found: %s/%s".formatted(author, name)));
        repository.delete(entity);
    }
}