package com.project.yamipick.waiting.domain;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;


/**
 * QWaitingLog is a Querydsl query type for WaitingLog
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QWaitingLog extends EntityPathBase<WaitingLog> {

    private static final long serialVersionUID = 1686551929L;

    public static final QWaitingLog waitingLog = new QWaitingLog("waitingLog");

    public final StringPath actionType = createString("actionType");

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final StringPath logMessage = createString("logMessage");

    public final DateTimePath<java.time.LocalDateTime> regDate = createDateTime("regDate", java.time.LocalDateTime.class);

    public final NumberPath<Long> storeId = createNumber("storeId", Long.class);

    public final NumberPath<Long> waitingId = createNumber("waitingId", Long.class);

    public QWaitingLog(String variable) {
        super(WaitingLog.class, forVariable(variable));
    }

    public QWaitingLog(Path<? extends WaitingLog> path) {
        super(path.getType(), path.getMetadata());
    }

    public QWaitingLog(PathMetadata metadata) {
        super(WaitingLog.class, metadata);
    }

}

