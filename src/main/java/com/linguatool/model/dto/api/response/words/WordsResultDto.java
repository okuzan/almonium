package com.linguatool.model.dto.api.response.words;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

import static lombok.AccessLevel.PRIVATE;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = PRIVATE)
public class WordsResultDto {
    String definition;
    String partOfSpeech;

    String[] synonyms;
    String[] antonyms;

    String[] derivation;
    String[] examples;

    String[] similarTo;
    String[] also;          // phrases of
    String[] entails;       // verbs
    String[] pertainsTo;

    String[] memberOf;
    String[] hasMembers;

    String[] substanceOf;
    String[] hasSubstances;

    String[] usageOf;
    String[] hasUsages;

    String[] inRegion;
    String[] regionOf;

    String[] typeOf;
    String[] hasTypes;

    String[] partOf;
    String[] hasParts;

    String[] instanceOf;
    String[] hasInstances;
}
