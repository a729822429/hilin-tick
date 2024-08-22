package icu.hilin.tick.server.repo;

public interface KVRepo {

    /**
     * 获取数据
     */
    String get(String key);

    /**
     * key是否存在
     */
    boolean containsKey(String key);

    /**
     * 写入
     * 如果key存在，就更新
     * 如果key不存在，就写入
     */
    void put(String key, String value);

    /**
     * 写入
     * 如果key存在，就更新
     * 如果key不存在，就写入
     *
     * @param expireSeconds 过期时间，单位秒
     */
    void put(String key, String value, Long expireSeconds);

    /**
     * 写入
     * 如果存在则更新，返回true
     * 如果不存在则报错，返回false
     */
    boolean putIfExist(String key, String value);

    /**
     * 写入
     * 如果存在则更新，返回true
     * 如果不存在则报错，返回false
     *
     * @param expireSeconds 过期时间，单位秒
     */
    boolean putIfExist(String key, String value, Long expireSeconds) throws Exception;

    /**
     * 写入
     * 如果不存在则写入，返回true
     * 如果存在则报错，返回false
     */
    boolean putIfNotExist(String key, String value) throws Exception;

    /**
     * 写入
     * 如果不存在则写入
     * 如果存在则报错
     *
     * @param expireSeconds 过期时间，单位秒
     */
    boolean putIfNotExist(String key, String value, Long expireSeconds) throws Exception;

    /**
     * 删除key
     */
    void del(String key);

    /**
     * 重新设置key过期时间
     *
     * @param expireSeconds 过期时间，单位秒
     */
    void ttl(String key, Long expireSeconds);

    /**
     * 获取过期时间，单位秒
     * 如果key不存在或者无过期时间，则返回null
     */
    Long getTtl(String key);

}
