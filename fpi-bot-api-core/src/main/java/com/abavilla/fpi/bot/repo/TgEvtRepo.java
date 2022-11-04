package com.abavilla.fpi.bot.repo;

import javax.enterprise.context.ApplicationScoped;

import com.abavilla.fpi.bot.entity.telegram.TelegramEvt;
import com.abavilla.fpi.fw.repo.AbsMongoRepo;

@ApplicationScoped
public class TgEvtRepo extends AbsMongoRepo<TelegramEvt> {
}
