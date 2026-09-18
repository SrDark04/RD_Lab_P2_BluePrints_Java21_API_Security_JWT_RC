package co.edu.eci.blueprints.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.Id;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.GenerationType;
import jakarta.persistence.JoinColumn;

@Entity
@Table(name = "blueprint_points")
public class BluePrintPoint {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private int x;
    private int y;

    @ManyToOne
    @JoinColumn(name = "blueprint_id", nullable = false)
    private BluePrint blueprint;

    protected BluePrintPoint() {
    }

    public BluePrintPoint(int x, int y, BluePrint blueprint) {
        this.x = x;
        this.y = y;
        this.blueprint = blueprint;
    }

    public Long getId() {
        return id;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public BluePrint getBlueprint() {
        return blueprint;
    }
}
