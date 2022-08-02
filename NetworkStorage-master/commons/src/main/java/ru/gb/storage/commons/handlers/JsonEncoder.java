package ru.gb.storage.commons.handlers;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageEncoder;
import ru.gb.storage.commons.messages.Message;

import java.util.List;

public class JsonEncoder extends MessageToMessageEncoder<Message> {
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    @Override
    protected void encode(ChannelHandlerContext ctx, Message msg, List<Object> out) throws Exception {
        String value = OBJECT_MAPPER.writeValueAsString(msg);
//        System.out.println("Out message: " + value);
        out.add(value);
    }
}