package com.xwm.xiaohongshucopywriteragent.store;

import com.xwm.xiaohongshucopywriteragent.bean.ChatMessages;
import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.data.message.ChatMessageDeserializer;
import dev.langchain4j.data.message.ChatMessageSerializer;
import dev.langchain4j.store.memory.chat.ChatMemoryStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Component;

import java.util.LinkedList;
import java.util.List;

@Component
public class MongoChatMemoryStore implements ChatMemoryStore
{
    @Autowired
    private MongoTemplate mongoTemplate;

    @Override
    public List<ChatMessage> getMessages(Object memoryId) {
        Criteria id = Criteria.where("memoryId").is(memoryId);
        Query query = new Query(id);
        ChatMessages messages = mongoTemplate.findOne(query, ChatMessages.class);
        if (messages == null) {
            return new LinkedList<>();
        }
        String content = messages.getContent();
        return ChatMessageDeserializer.messagesFromJson(content);
    }

    /*
    * list 聊天记录列表
    * */
    @Override
    public void updateMessages(Object memoryId, List<ChatMessage> list) {
        Criteria id = Criteria.where("memoryId").is(memoryId);
        Query query = new Query(id);
        Update update = new Update();
        update.set("content", ChatMessageSerializer.messagesToJson(list));
        mongoTemplate.upsert(query, update, ChatMessages.class);
    }

    @Override
    public void deleteMessages(Object memoryId) {
        Criteria id = Criteria.where("_id").is(memoryId);
        Query query = new Query(id);
        mongoTemplate.remove(query, ChatMessages.class);
    }
}
