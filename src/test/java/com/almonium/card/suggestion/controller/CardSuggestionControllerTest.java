package com.almonium.card.suggestion.controller;

import static lombok.AccessLevel.PRIVATE;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.almonium.auth.common.model.entity.Principal;
import com.almonium.base.BaseControllerTest;
import com.almonium.card.suggestion.dto.CardSuggestionDto;
import com.almonium.card.suggestion.service.CardSuggestionService;
import com.almonium.user.core.model.entity.Learner;
import com.almonium.util.TestDataGenerator;
import lombok.SneakyThrows;
import lombok.experimental.FieldDefaults;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(CardSuggestionController.class)
@FieldDefaults(level = PRIVATE)
@AutoConfigureMockMvc(addFilters = false)
class CardSuggestionControllerTest extends BaseControllerTest {
    private static final String BASE_URL = "/cards/suggestions";
    private static final String ACCEPT_CARD_URL = BASE_URL + "/{id}/accept";
    private static final String DECLINE_CARD_URL = BASE_URL + "/{id}/decline";

    @Autowired
    MockMvc mockMvc;

    @MockBean
    CardSuggestionService cardSuggestionService;

    Learner learner;

    @BeforeEach
    void setUp() {
        Principal principal = TestDataGenerator.buildTestPrincipal();
        learner = principal.getUser().getLearner();
        SecurityContextHolder.getContext().setAuthentication(TestDataGenerator.getAuthenticationToken(principal));
    }

    @DisplayName("Should suggest card")
    @Test
    @SneakyThrows
    void givenCardSuggestionDto_whenSuggestCard_thenCardSuggestionCreated() {
        CardSuggestionDto dto = new CardSuggestionDto(1L, 2L);

        mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated());
    }

    @DisplayName("Should accept a card suggestion for current user")
    @Test
    @SneakyThrows
    void givenCardId_whenAcceptCard_thenSuggestionAccepted() {
        Long cardIdToAccept = 1L;

        mockMvc.perform(put(ACCEPT_CARD_URL, cardIdToAccept).contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent());

        verify(cardSuggestionService).acceptSuggestion(cardIdToAccept, learner);
    }

    @DisplayName("Should decline a card suggestion for current user")
    @Test
    @SneakyThrows
    void givenCardId_whenDeclineCard_thenSuggestionDeclined() {
        Long cardIdToDecline = 1L;

        mockMvc.perform(put(DECLINE_CARD_URL, cardIdToDecline).contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent());

        verify(cardSuggestionService).declineSuggestion(cardIdToDecline, learner);
    }

    @DisplayName("Should retrieve suggested cards for a user")
    @Test
    @SneakyThrows
    void givenUser_whenGetSuggestedCards_thenReturnsCards() {
        mockMvc.perform(get(BASE_URL)).andExpect(status().isOk());

        verify(cardSuggestionService).getSuggestedCards(eq(learner));
    }
}
