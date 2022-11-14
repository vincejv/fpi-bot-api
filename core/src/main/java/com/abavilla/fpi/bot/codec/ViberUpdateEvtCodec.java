package com.abavilla.fpi.bot.codec;

import com.abavilla.fpi.fw.util.MapperUtil;
import com.abavilla.fpi.viber.ext.dto.ViberUpdate;
import com.fasterxml.jackson.databind.ObjectReader;
import com.fasterxml.jackson.databind.ObjectWriter;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.eventbus.MessageCodec;
import lombok.SneakyThrows;

public class ViberUpdateEvtCodec implements MessageCodec<ViberUpdate, ViberUpdate> {

  @SneakyThrows
  @Override
  public void encodeToWire(Buffer buffer, ViberUpdate update) {
    ObjectWriter writer = MapperUtil.mapper().writerFor(ViberUpdate.class);
    byte[] bytes = writer.writeValueAsBytes(update);
    buffer.appendInt(bytes.length);
    buffer.appendBytes(bytes);
  }

  @SneakyThrows
  @Override
  public ViberUpdate decodeFromWire(int pos, Buffer buffer) {
    // My custom message starting from this *position* of buffer

    // Length of JSON
    int length = buffer.getInt(pos);

    // Get JSON string by it`s length
    // Jump 4 because getInt() == 4 bytes
    pos += 4;
    byte[] bytes = buffer.getBytes(pos, pos + length);

    ObjectReader reader = MapperUtil.mapper().readerFor(ViberUpdate.class);
    return reader.readValue(bytes);
  }

  @Override
  public ViberUpdate transform(ViberUpdate update) {
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
