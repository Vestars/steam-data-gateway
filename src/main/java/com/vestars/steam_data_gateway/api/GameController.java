package com.vestars.steam_data_gateway.api;

import com.vestars.steam_data_gateway.dto.GameDto;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.List;

@RestController
@RequestMapping("/games")
public class GameController {
    private static final String QUEUE = "steam-game-queue";
    private static final String RETRIEVAL_SERVICE_URL = "http://localhost:8800/games";

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Autowired
    private WebClient webClient;

    @PostMapping
    public ResponseEntity<String> createGame(@RequestBody GameDto gameDto) {
        rabbitTemplate.convertAndSend(QUEUE, gameDto);
        return ResponseEntity.ok("Game sent to queue.");
    }

    @GetMapping
    public ResponseEntity<List<GameDto>> getAllGames() {
        List<GameDto> games = webClient.get()
                .uri(RETRIEVAL_SERVICE_URL)
                .retrieve()
                .bodyToFlux(GameDto.class)
                .collectList()
                .block();

        return ResponseEntity.ok(games);
    }
}
