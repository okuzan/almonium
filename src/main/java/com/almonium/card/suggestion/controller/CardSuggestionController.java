package com.almonium.card.suggestion.controller;

import static lombok.AccessLevel.PRIVATE;

import com.almonium.auth.common.annotation.Auth;
import com.almonium.card.core.dto.CardDto;
import com.almonium.card.suggestion.dto.CardSuggestionDto;
import com.almonium.card.suggestion.service.CardSuggestionService;
import com.almonium.user.core.model.entity.User;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/cards/suggestions")
@FieldDefaults(level = PRIVATE, makeFinal = true)
@RequiredArgsConstructor
public class CardSuggestionController {
    CardSuggestionService cardSuggestionService;

    @PostMapping
    public ResponseEntity<?> suggestCard(@Valid @RequestBody CardSuggestionDto dto, @Auth User user) {
        cardSuggestionService.suggestCard(dto, user.getLearner());
        return new ResponseEntity<>(HttpStatus.CREATED);
    }

    @PutMapping("/{id}/accept")
    public ResponseEntity<Void> acceptCard(@PathVariable Long id, @Auth User user) {
        cardSuggestionService.acceptSuggestion(id, user.getLearner());
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{id}/decline")
    public ResponseEntity<Void> declineCard(@PathVariable Long id, @Auth User user) {
        cardSuggestionService.declineSuggestion(id, user.getLearner());
        return ResponseEntity.noContent().build();
    }

    @GetMapping
    public ResponseEntity<List<CardDto>> getSuggestedCardStack(@Auth User user) {
        return ResponseEntity.ok(cardSuggestionService.getSuggestedCards(user.getLearner()));
    }
}
