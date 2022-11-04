package com.abavilla.fpi.bot.ext.entity.enums;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;

import com.abavilla.fpi.fw.entity.enums.IBaseEnum;
import com.abavilla.fpi.fw.util.FWConst;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import io.quarkus.runtime.annotations.RegisterForReflection;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
@RegisterForReflection
public enum BotSource implements IBaseEnum {
  FB_MSGR(1, "Facebook Messenger"),
  TELEGRAM(2, "Telegram"),
  VIBER(3, "Viber"),
  WHATSAPP(4, "WhatsApp"),
  SIGNAL(5, "Signal"),
  WECHAT(6, "WeChat"),
  SLACK(7, "Slack"),
  MS_TEAMS(8, "Microsoft Teams"),
  LINE(9, "Line"),
  KIK(10, "Kik"),
  IMESSAGE(11, "iMessage"),
  UNKNOWN(-1, FWConst.UNKNOWN_PREFIX);

  /**
   * Ordinal id to enum mapping
   */
  private static final Map<Integer, IBaseEnum> ENUM_MAP = new HashMap<>();

  static {
    for(IBaseEnum w : EnumSet.allOf(BotSource.class))
      ENUM_MAP.put(w.getId(), w);
  }

  /**
   * The enum ordinal id
   */
  private final int id;

  /**
   * The enum value
   */
  private final String value;

  /**
   * Creates an enum based from given string value
   *
   * @param value the string value
   * @return the created enum
   */
  @JsonCreator(mode = JsonCreator.Mode.DELEGATING)
  public static BotSource fromValue(String value) {
    return (BotSource) IBaseEnum.fromValue(value, ENUM_MAP, UNKNOWN);
  }

  /**
   * Creates an enum based from given an ordinal id
   *
   * @param id the ordinal id
   * @return the created enum
   */
  public static BotSource fromId(int id) {
    return (BotSource) IBaseEnum.fromId(id, ENUM_MAP, UNKNOWN);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  @JsonValue
  public String toString() {
    return value;
  }
}
