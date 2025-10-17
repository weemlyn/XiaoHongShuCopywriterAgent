package com.xwm.xiaohongshucopywriteragent.controller;

import com.xwm.xiaohongshucopywriteragent.assistant.MingAgent;
import com.xwm.xiaohongshucopywriteragent.bean.ChatForm;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

@Tag(name = "小明智能体")
@RestController
@RequestMapping("/mingAgent")
public class MingAgentChatController {

    @Autowired
    private MingAgent mingAgent;

    @PostMapping(value = "/chat",produces = "text/stream;charset=utf-8")
    public Flux<String> chat(@RequestBody ChatForm chatForm) {
        return mingAgent.chat(chatForm.getMemoryID(),chatForm.getUserMassage());
    }
}
