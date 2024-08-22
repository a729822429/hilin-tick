package icu.hilin.tick.server.entity.po;

import lombok.Data;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

/**
 * kv存储
 */
@Data
@Document("hilin-kv")
public class KVPO extends BasePO {

    /**
     * 存储的键名
     */
    @Indexed(unique = true)
    private String key;

    /**
     * 存储的键值
     */
    private String value;

}
