package com.linguatool.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CardAcceptanceDto {

    @NotNull
    Long senderId;

    @NotNull
    Long cardId;
}