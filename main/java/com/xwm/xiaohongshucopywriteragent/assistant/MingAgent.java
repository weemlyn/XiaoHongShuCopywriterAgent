package com.xwm.xiaohongshucopywriteragent.assistant;


import dev.langchain4j.service.MemoryId;
import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;
import dev.langchain4j.service.spring.AiService;
import reactor.core.publisher.Flux;

import static dev.langchain4j.service.spring.AiServiceWiringMode.EXPLICIT;

@AiService(
        wiringMode = EXPLICIT,
        streamingChatModel = "qwenStreamingChatModel",
        chatMemoryProvider = "mingChatMemoryProvider",
        tools = "appointmentTools",
        contentRetriever = "mingContentRetriever"

        )
public interface MingAgent {

    @SystemMessage(fromResource = "MingPrompt.txt")
    Flux<String> chat(@MemoryId int memoryID, @UserMessage String usermassage);
}
