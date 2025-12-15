package com.project.yamipick.log.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;


/**
 * QBusinessLog is a Querydsl query type for BusinessLog
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QBusinessLog extends EntityPathBase<BusinessLog> {

    private static final long serialVersionUID = -283778646L;

    public static final QBusinessLog businessLog = new QBusinessLog("businessLog");

    public final StringPath actionType = createString("actionType");

    public final DateTimePath<java.time.LocalDateTime> createdAt = createDateTime("createdAt", java.time.LocalDateTime.class);

    public final StringPath ipAddr = createString("ipAddr");

    public final StringPath message = createString("message");

    public final NumberPath<Long> seqLog = createNumber("seqLog", Long.class);

    public final StringPath targetId = createString("targetId");

    public final StringPath targetType = createString("targetType");

    public final StringPath userId = createString("userId");

    public QBusinessLog(String variable) {
        super(BusinessLog.class, forVariable(variable));
    }

    public QBusinessLog(Path<? extends BusinessLog> path) {
        super(path.getType(), path.getMetadata());
    }

    public QBusinessLog(PathMetadata metadata) {
        super(BusinessLog.class, metadata);
    }

}

