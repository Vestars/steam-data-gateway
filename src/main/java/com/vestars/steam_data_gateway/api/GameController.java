package com.vestars.steam_data_gateway.api;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
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
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Autowired
    private WebClient webClient;

    @PostMapping
    public ResponseEntity<Void> createGame(@RequestBody GameDto gameDto) throws JsonProcessingException {
        String gameDtoJson = this.objectMapper.writeValueAsString(gameDto);
        rabbitTemplate.convertAndSend(QUEUE, gameDtoJson);
        return ResponseEntity.ok().build();
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
