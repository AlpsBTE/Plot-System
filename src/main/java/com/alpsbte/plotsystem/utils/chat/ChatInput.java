package com.alpsbte.plotsystem.utils.chat;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public interface ChatInput {
    Map<UUID, ChatInput> awaitChatInput = new HashMap<>();

    LocalDateTime getDateTime();
}
