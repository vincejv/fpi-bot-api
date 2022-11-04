package com.abavilla.fpi.bot.entity.telegram;

import java.time.LocalDateTime;

import com.abavilla.fpi.fw.entity.AbsItem;
import io.quarkus.mongodb.panache.common.MongoEntity;
import io.quarkus.runtime.annotations.RegisterForReflection;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@NoArgsConstructor
@RegisterForReflection
@MongoEntity(collection = "telegram_event")
public class TelegramEvt extends AbsItem {
  private Integer updateId;
  private Long senderId;
  private Integer messageId;
  private Integer replyTo;
  private String content;
  private LocalDateTime timestamp;
}
