package icu.hilin.tick.server.entity.po;

import lombok.Data;
import org.springframework.data.annotation.Id;

@Data
public class BasePO {

    @Id
    private String id;

    /**
     * 过期时间戳，毫秒
     */
    private Long expireTimestamp;

    /**
     * 创建时间戳，毫秒
     */
    private Long createTimestamp;

    /**
     * 创建时间，精确度：毫秒,格式：yyyy-MM-dd HH:mm:ss.SSS
     */
    private String createDateTimeMillisecond;

    /**
     * 创建时间，精确度:秒,格式：yyyy-MM-dd HH:mm:ss
     */
    private String createDateTime;

}
