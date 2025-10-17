package com.xwm.xiaohongshucopywriteragent.bean;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Document("chat_messages")
public class ChatMessages {

    //唯一标识，用来映射mangodb的_id字段
    @Id
    private ObjectId messageId;

    private String memoryId;

    private String content;//存储当前聊天记录列表的json字段
}
