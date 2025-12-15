package com.project.yamipick.waiting.domain;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QWaiting is a Querydsl query type for Waiting
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QWaiting extends EntityPathBase<Waiting> {

    private static final long serialVersionUID = 1716688331L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QWaiting waiting = new QWaiting("waiting");

    public final DateTimePath<java.time.LocalDateTime> calledTime = createDateTime("calledTime", java.time.LocalDateTime.class);

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final QWaitingOperation operation;

    public final DateTimePath<java.time.LocalDateTime> regDate = createDateTime("regDate", java.time.LocalDateTime.class);

    public final DateTimePath<java.time.LocalDateTime> seatedTime = createDateTime("seatedTime", java.time.LocalDateTime.class);

    public final NumberPath<Integer> teamSize = createNumber("teamSize", Integer.class);

    public final com.project.yamipick.user.entity.QUser user;

    public final NumberPath<Integer> waitingNumber = createNumber("waitingNumber", Integer.class);

    public final QWaitingStatus waitingStatus;

    public QWaiting(String variable) {
        this(Waiting.class, forVariable(variable), INITS);
    }

    public QWaiting(Path<? extends Waiting> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QWaiting(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QWaiting(PathMetadata metadata, PathInits inits) {
        this(Waiting.class, metadata, inits);
    }

    public QWaiting(Class<? extends Waiting> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.operation = inits.isInitialized("operation") ? new QWaitingOperation(forProperty("operation"), inits.get("operation")) : null;
        this.user = inits.isInitialized("user") ? new com.project.yamipick.user.entity.QUser(forProperty("user")) : null;
        this.waitingStatus = inits.isInitialized("waitingStatus") ? new QWaitingStatus(forProperty("waitingStatus")) : null;
    }

}

