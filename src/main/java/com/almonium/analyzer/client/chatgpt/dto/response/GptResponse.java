package com.almonium.analyzer.client.chatgpt.dto.response;

import com.almonium.analyzer.client.chatgpt.dto.common.Message;
import java.util.List;

public record GptResponse(List<Choice> choices) {
    public record Choice(int index, Message message) {}
}
