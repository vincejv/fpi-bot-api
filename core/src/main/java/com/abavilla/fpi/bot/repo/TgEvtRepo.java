package com.abavilla.fpi.bot.repo;

import com.abavilla.fpi.bot.entity.telegram.TelegramEvt;
import com.abavilla.fpi.fw.repo.AbsMongoRepo;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class TgEvtRepo extends AbsMongoRepo<TelegramEvt> {
}
