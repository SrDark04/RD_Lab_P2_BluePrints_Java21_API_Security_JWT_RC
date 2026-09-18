package co.edu.eci.blueprints.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.Id;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.OneToMany;
import jakarta.persistence.CascadeType;
import java.util.*;

@Entity
@Table(name = "blueprints")
public class BluePrint {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String author;
    private String name;

    @OneToMany(mappedBy = "blueprint", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<BluePrintPoint> points = new ArrayList<>();

    protected BluePrint() {
    }

    public BluePrint(String author, String name) {
        this.author = author;
        this.name = name;
    }

    public Long getId() {
        return id;
    }

    public String getAuthor() {
        return author;
    }

    public String getName() {
        return name;
    }

    public List<BluePrintPoint> getPoints() {
        return points;
    }

    public void addPoint(int x, int y) {
        points.add(new BluePrintPoint(x, y, this));
    }

}
