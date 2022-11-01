package com.abavilla.fpi.bot.entity.enums;

import static com.abavilla.fpi.fw.util.FWConst.UNKNOWN_PREFIX;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;

import com.abavilla.fpi.fw.entity.enums.IBaseEnum;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import io.quarkus.runtime.annotations.RegisterForReflection;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
@RegisterForReflection
public enum SenderAction implements IBaseEnum {
  MARK_SEEN(1, "MARK_SEEN"),
  TYPING_ON(2, "TYPING_ON"),
  TYPING_OFF(3, "TYPING_OFF"),
  REACT(4, "REACT"),
  UNREACT(5, "UNREACT"),
  UNKNOWN(-1, UNKNOWN_PREFIX);

  /**
   * Ordinal id to enum mapping
   */
  private static final Map<Integer, IBaseEnum> ENUM_MAP = new HashMap<>();

  static {
    for(IBaseEnum w : EnumSet.allOf(SenderAction.class))
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
  public static SenderAction fromValue(String value) {
    return (SenderAction) IBaseEnum.fromValue(value, ENUM_MAP, UNKNOWN);
  }

  /**
   * Creates an enum based from given an ordinal id
   *
   * @param id the ordinal id
   * @return the created enum
   */
  public static SenderAction fromId(int id) {
    return (SenderAction) IBaseEnum.fromId(id, ENUM_MAP, UNKNOWN);
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
