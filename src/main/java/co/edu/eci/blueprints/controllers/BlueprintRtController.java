package co.edu.eci.blueprints.controllers;

import co.edu.eci.blueprints.services.BlueprintsServices;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

@Controller
public class BlueprintRtController {

    private static final Logger log = LoggerFactory.getLogger(BlueprintRtController.class);

    private final SimpMessagingTemplate broker;
    private final BlueprintsServices services;

    public BlueprintRtController(SimpMessagingTemplate broker, BlueprintsServices services) {
        this.broker = broker;
        this.services = services;
    }

    @MessageMapping("/draw")
    public void handleDraw(@Payload DrawPayload payload) {
        log.info("Draw Event: {}.{} -> point=({},{})",
                payload.author(), payload.name(),
                payload.point().x(), payload.point().y());

        try {
            services.addPoint(payload.author(), payload.name(), payload.point().x(), payload.point().y());
        } catch (Exception e) {
            log.warn("No se puede persistir el punto: {}", e.getMessage());
        }

        broker.convertAndSend(
                "/topic/blueprints." + payload.author() + "." + payload.name(), payload);
    }

    public record DrawPayload(String author, String name, PointPayload point) {
    }

    public record PointPayload(int x, int y) {
    }

}
