package com.abavilla.fpi.bot.codec;

import com.abavilla.fpi.fw.util.MapperUtil;
import com.fasterxml.jackson.databind.ObjectReader;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.pengrad.telegrambot.model.Update;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.eventbus.MessageCodec;
import lombok.SneakyThrows;

public class TGUpdateEvtCodec implements MessageCodec<Update, Update> {

  @SneakyThrows
  @Override
  public void encodeToWire(Buffer buffer, Update update) {
    ObjectWriter writer = MapperUtil.mapper().writerFor(Update.class);
    byte[] bytes = writer.writeValueAsBytes(update);
    buffer.appendInt(bytes.length);
    buffer.appendBytes(bytes);
  }

  @SneakyThrows
  @Override
  public Update decodeFromWire(int pos, Buffer buffer) {
    // My custom message starting from this *position* of buffer

    // Length of JSON
    int length = buffer.getInt(pos);

    // Get JSON string by it`s length
    // Jump 4 because getInt() == 4 bytes
    pos += 4;
    byte[] bytes = buffer.getBytes(pos, pos + length);

    ObjectReader reader = MapperUtil.mapper().readerFor(Update.class);
    return reader.readValue(bytes);
  }

  @Override
  public Update transform(Update update) {
    return update;
  }

  @Override
  public String name() {
    return this.getClass().getName();
  }

  @Override
  public byte systemCodecID() {
    return -1;
  }
}
