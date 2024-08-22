package icu.hilin.tick.server.repo.impl;

import icu.hilin.tick.server.entity.po.KVPO;
import icu.hilin.tick.server.repo.KVRepo;
import lombok.AllArgsConstructor;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Component;

/**
 * fixme 有问题，还未实现
 */
@Component
@AllArgsConstructor
public class KVRepoImpl implements KVRepo {

    private final MongoTemplate mongoTemplate;

    @Override
    public String get(String key) {
        try {
            return mongoTemplate.findOne(queryByKey(key), KVPO.class).getValue();
        } catch (Exception e) {
            return null;
        }
    }

    @Override
    public boolean containsKey(String key) {
        try {
            return get(key) != null;
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public void put(String key, String value) {
        put(key, value, null);
    }

    @Override
    public void put(String key, String value, Long expireSeconds) {
        del(key);
        KVPO kvpo = new KVPO();
        kvpo.setKey(key);
        kvpo.setValue(value);
        if (expireSeconds != null) {
            kvpo.setExpireTimestamp(System.currentTimeMillis() + expireSeconds * 1000);
        }
        mongoTemplate.save(kvpo);
    }

    @Override
    public boolean putIfExist(String key, String value) {
        return putIfExist(key, value, null);
    }

    @Override
    public boolean putIfExist(String key, String value, Long expireSeconds) {
        if (containsKey(key)) {
            if (expireSeconds != null) {
                mongoTemplate.updateFirst(queryByKey(key), Update.update("value", value).set("expireTimestamp", System.currentTimeMillis() + expireSeconds * 1000), KVPO.class);
            } else {
                mongoTemplate.updateFirst(queryByKey(key), Update.update("value", value), KVPO.class);
            }
            return true;
        }
        return false;
    }

    @Override
    public boolean putIfNotExist(String key, String value) {
        return putIfNotExist(key, value, null);
    }

    @Override
    public boolean putIfNotExist(String key, String value, Long expireSeconds) {
        if (!containsKey(key)) {
            KVPO kvpo = new KVPO();
            kvpo.setKey(key);
            kvpo.setValue(value);
            if (expireSeconds != null) {
                kvpo.setExpireTimestamp(System.currentTimeMillis() + expireSeconds * 1000);
            }
            mongoTemplate.save(kvpo);
            return true;
        }
        return false;
    }

    @Override
    public void del(String key) {
        mongoTemplate.remove(queryByKey(key), KVPO.class);
    }

    @Override
    public void ttl(String key, Long expireSeconds) {
        if (containsKey(key) && expireSeconds != null) {

        }
    }

    @Override
    public Long getTtl(String key) {
        return null;
    }

    private static Query queryByKey(String key) {
        return Query.query(Criteria.where("key").is(key).and("expireTimestamp").lt(System.currentTimeMillis()));
    }
}
