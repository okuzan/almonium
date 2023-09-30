package com.linguarium.client.oxford.dto;

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
public class OxfordEntry {
    String[] etymologies;
    OxfordNote[] notes;
    OxfordPronunciation[] pronunciations;
    OxfordSense[] senses;
}