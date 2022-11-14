package com.abavilla.fpi.bot.repo;

import javax.enterprise.context.ApplicationScoped;

import com.abavilla.fpi.bot.entity.viber.ViberEvt;
import com.abavilla.fpi.fw.repo.AbsMongoRepo;

@ApplicationScoped
public class ViberEvtRepo extends AbsMongoRepo<ViberEvt> {
}
