package com.almonium.analyzer.translator.model.entity.pk;

import com.almonium.analyzer.translator.model.enums.Language;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@EqualsAndHashCode
@NoArgsConstructor
@AllArgsConstructor
public class TranslatorMappingKey {
    private Language sourceLang;
    private Language targetLang;
    private Long translatorId;
}
