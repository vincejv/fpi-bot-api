package com.abavilla.fpi.bot.ext.codec;

import com.abavilla.fpi.bot.ext.entity.enums.BotSource;
import com.abavilla.fpi.fw.config.codec.AbsEnumCodec;

/**
 * MongoDB Codec for {@link BotSource} enum.
 *
 * @author <a href="mailto:vincevillamora@gmail.com">Vince Villamora</a>
 */
public class BotSourceCodec extends AbsEnumCodec<BotSource> {

  /**
   * {@inheritDoc}
   */
  @Override
  public Class<BotSource> getEncoderClass() {
    return BotSource.class;
  }
}
