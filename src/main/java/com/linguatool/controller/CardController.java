package com.linguatool.controller;

import com.linguatool.annotation.CurrentUser;
import com.linguatool.model.dto.LocalUser;
import com.linguatool.model.dto.external_api.request.CardCreationDto;
import com.linguatool.model.dto.external_api.request.CardDto;
import com.linguatool.model.dto.external_api.request.CardUpdateDto;
import com.linguatool.service.CardService;
import com.linguatool.service.CardSuggestionService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("api/cards")
public class CardController {
    private final CardService cardService;
    private final CardSuggestionService cardSuggestionService;

    public CardController(CardService cardService, CardSuggestionService cardSuggestionService) {
        this.cardService = cardService;
        this.cardSuggestionService = cardSuggestionService;
    }

    @PostMapping("create")
    public ResponseEntity<?> createCard(@Valid @RequestBody CardCreationDto dto, @CurrentUser LocalUser userDetails) {
        cardService.createCard(userDetails.getUser(), dto);
        return ResponseEntity.ok().build();
    }

    @PatchMapping("update")
    public ResponseEntity<?> updateCard(@Valid @RequestBody CardUpdateDto dto, @CurrentUser LocalUser user) {
        cardService.updateCard(dto, user.getUser());
        return ResponseEntity.ok().build();
    }

    @GetMapping("all")
    public ResponseEntity<List<CardDto>> getCardStack(@CurrentUser LocalUser user) {
        return ResponseEntity.ok(cardService.getUsersCards(user.getUser()));
    }

    @GetMapping("all/{code}")
    public ResponseEntity<List<CardDto>> getCardStackOfLang(@PathVariable String code, @CurrentUser LocalUser user) {
        return ResponseEntity.ok(cardService.getUsersCardsOfLang(code, user.getUser()));
    }

    @GetMapping("suggested")
    public ResponseEntity<List<CardDto>> getSuggestedCardStack(@CurrentUser LocalUser user) {
        return ResponseEntity.ok(cardSuggestionService.getSuggestedCards(user.getUser()));
    }

    @GetMapping("{id}")
    public ResponseEntity<CardDto> getCard(@PathVariable Long id) {
        return ResponseEntity.ok(cardService.getCardById(id));
    }

    @GetMapping("hash/{hash}")
    public ResponseEntity<CardDto> getCardByHash(@PathVariable String hash) {
        return ResponseEntity.ok(cardService.getCardByHash(hash));
    }

    @DeleteMapping("{id}")
    public ResponseEntity<CardDto> deleteCard(@PathVariable Long id) {
        cardService.deleteById(id);
        return ResponseEntity.ok().build();
    }
}
