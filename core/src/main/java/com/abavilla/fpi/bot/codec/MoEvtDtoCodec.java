package com.abavilla.fpi.bot.codec;

import com.abavilla.fpi.bot.dto.MOEvtDto;
import com.abavilla.fpi.fw.util.MapperUtil;
import com.fasterxml.jackson.databind.ObjectReader;
import com.fasterxml.jackson.databind.ObjectWriter;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.eventbus.MessageCodec;
import lombok.SneakyThrows;

public class MoEvtDtoCodec implements MessageCodec<MOEvtDto, MOEvtDto> {

  @SneakyThrows
  @Override
  public void encodeToWire(Buffer buffer, MOEvtDto dtoEvt) {
    ObjectWriter writer = MapperUtil.mapper().writerFor(MOEvtDto.class);
    byte[] bytes = writer.writeValueAsBytes(dtoEvt);
    buffer.appendInt(bytes.length);
    buffer.appendBytes(bytes);
  }

  @SneakyThrows
  @Override
  public MOEvtDto decodeFromWire(int pos, Buffer buffer) {
    // My custom message starting from this *position* of buffer

    // Length of JSON
    int length = buffer.getInt(pos);

    // Get JSON string by it`s length
    // Jump 4 because getInt() == 4 bytes
    pos += 4;
    byte[] bytes = buffer.getBytes(pos, pos + length);

    ObjectReader reader = MapperUtil.mapper().readerFor(MOEvtDto.class);
    return reader.readValue(bytes);
  }

  @Override
  public MOEvtDto transform(MOEvtDto dtoEvt) {
    return dtoEvt;
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
