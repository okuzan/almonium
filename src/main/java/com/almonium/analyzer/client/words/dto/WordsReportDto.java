package com.almonium.analyzer.client.words.dto;

import static lombok.AccessLevel.PRIVATE;

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
public class WordsReportDto {
    String word;
    Double frequency;
    WordsResultDto[] results;
    WordsSyllablesDto syllables;
    WordsPronunciationDto pronunciation;
}
