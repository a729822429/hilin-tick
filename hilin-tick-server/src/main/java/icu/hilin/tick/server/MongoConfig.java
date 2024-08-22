package icu.hilin.tick.server;

import cn.hutool.core.date.DateUtil;
import icu.hilin.tick.server.entity.po.BasePO;
import org.springframework.context.annotation.Bean;
import org.springframework.data.mongodb.MongoDatabaseFactory;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.convert.DefaultDbRefResolver;
import org.springframework.data.mongodb.core.convert.DefaultMongoTypeMapper;
import org.springframework.data.mongodb.core.convert.MappingMongoConverter;
import org.springframework.data.mongodb.core.mapping.MongoMappingContext;
import org.springframework.data.mongodb.core.mapping.event.AbstractMongoEventListener;
import org.springframework.data.mongodb.core.mapping.event.BeforeSaveEvent;
import org.springframework.stereotype.Component;

import java.util.Date;

@Component
public class MongoConfig extends AbstractMongoEventListener<BasePO> {

    private static final String DATE_FORMAT_MI = "yyyy-MM-dd HH:mm:ss.SSS";
    private static final String DATE_FORMAT = "yyyy-MM-dd HH:mm:ss";

    @Bean
    public MongoTemplate mongoTemplate(MongoDatabaseFactory factory, MongoMappingContext context) {
        DefaultDbRefResolver refResolver = new DefaultDbRefResolver(factory);
        MappingMongoConverter converter = new MappingMongoConverter(refResolver, context);
        converter.setTypeMapper(new DefaultMongoTypeMapper(null));
        return new MongoTemplate(factory, converter);
    }

    @Override
    public void onBeforeSave(BeforeSaveEvent<BasePO> event) {
        super.onBeforeSave(event);
        Date current = new Date();
        // 创建时间
        event.getSource().setCreateTimestamp(current.getTime());
        event.getSource().setCreateDateTimeMillisecond(DateUtil.format(current, DATE_FORMAT_MI));
        event.getSource().setCreateDateTime(DateUtil.format(current, DATE_FORMAT));
    }

}
