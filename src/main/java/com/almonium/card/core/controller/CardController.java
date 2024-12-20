package com.almonium.card.core.controller;

import static lombok.AccessLevel.PRIVATE;

import com.almonium.analyzer.translator.model.enums.Language;
import com.almonium.auth.common.annotation.Auth;
import com.almonium.card.core.dto.CardCreationDto;
import com.almonium.card.core.dto.CardDto;
import com.almonium.card.core.dto.CardUpdateDto;
import com.almonium.card.core.service.CardService;
import com.almonium.user.core.model.entity.User;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/cards")
@RequiredArgsConstructor
@FieldDefaults(level = PRIVATE, makeFinal = true)
public class CardController {
    CardService cardService;

    @PostMapping
    public ResponseEntity<Void> createCard(@Valid @RequestBody CardCreationDto dto, @Auth User user) {
        cardService.createCard(user.getLearner(), dto);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @PutMapping("/{id}")
    public ResponseEntity<Void> updateCard(
            @PathVariable Long id, @Valid @RequestBody CardUpdateDto dto, @Auth User user) {
        cardService.updateCard(id, dto, user.getLearner());
        return ResponseEntity.ok().build();
    }

    @GetMapping
    public ResponseEntity<List<CardDto>> getCardStack(@Auth User user) {
        return ResponseEntity.ok(cardService.getUsersCards(user.getLearner()));
    }

    @GetMapping("/lang/{lang}")
    public ResponseEntity<List<CardDto>> getCardStackOfLang(@PathVariable Language lang, @Auth User user) {
        return ResponseEntity.ok(cardService.getUsersCardsOfLang(lang, user.getLearner()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<CardDto> getCard(@PathVariable Long id) {
        return ResponseEntity.ok(cardService.getCardById(id));
    }

    @GetMapping("/hash/{hash}")
    public ResponseEntity<CardDto> getCardByHash(@PathVariable String hash) {
        return ResponseEntity.ok(cardService.getCardByPublicId(hash));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCard(@PathVariable Long id) {
        cardService.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
