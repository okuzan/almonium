package com.almonium.card.core.dto;

import static lombok.AccessLevel.PRIVATE;

import jakarta.validation.constraints.NotNull;
import java.time.Instant;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = PRIVATE)
public class CardUpdateDto {
    @NotNull
    Long id;

    String entry;
    TranslationDto[] translations;
    String notes;
    TagDto[] tags;
    ExampleDto[] examples;
    Instant createdAt;
    Instant lastRepeat;
    Integer iteration;
    Long userId;
    Integer priority;
    int[] deletedTranslationsIds;
    int[] deletedExamplesIds;
    Instant updatedAt;
    Boolean activeLearning;
    Boolean falseFriend;
    Boolean irregularPlural;
    Boolean irregularSpelling;
    String language;
}
