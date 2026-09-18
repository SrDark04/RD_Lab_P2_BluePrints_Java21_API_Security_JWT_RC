package co.edu.eci.blueprints.persistence;

import co.edu.eci.blueprints.entity.BluePrint;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.*;

@Repository
public interface SpringDataBlueprintRepository extends JpaRepository<BluePrint, Long> {
    Optional<BluePrint> findByAuthorAndName(String author, String name);

}
