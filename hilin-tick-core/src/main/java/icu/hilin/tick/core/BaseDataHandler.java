package icu.hilin.tick.core;

import icu.hilin.tick.core.entity.BaseEntity;
import org.springframework.context.event.EventListener;

public abstract class BaseDataHandler<T extends BaseEntity<?>> {

    @EventListener
    abstract public void handler(T entity);

}
