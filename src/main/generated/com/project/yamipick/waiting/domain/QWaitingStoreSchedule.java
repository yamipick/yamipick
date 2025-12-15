package com.project.yamipick.waiting.domain;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QWaitingStoreSchedule is a Querydsl query type for WaitingStoreSchedule
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QWaitingStoreSchedule extends EntityPathBase<WaitingStoreSchedule> {

    private static final long serialVersionUID = -1572922739L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QWaitingStoreSchedule waitingStoreSchedule = new QWaitingStoreSchedule("waitingStoreSchedule");

    public final StringPath breakEnd = createString("breakEnd");

    public final StringPath breakStart = createString("breakStart");

    public final StringPath closeTime = createString("closeTime");

    public final NumberPath<Integer> dayOfWeek = createNumber("dayOfWeek", Integer.class);

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final StringPath isOpen = createString("isOpen");

    public final StringPath openTime = createString("openTime");

    public final QWaitingStore store;

    public QWaitingStoreSchedule(String variable) {
        this(WaitingStoreSchedule.class, forVariable(variable), INITS);
    }

    public QWaitingStoreSchedule(Path<? extends WaitingStoreSchedule> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QWaitingStoreSchedule(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QWaitingStoreSchedule(PathMetadata metadata, PathInits inits) {
        this(WaitingStoreSchedule.class, metadata, inits);
    }

    public QWaitingStoreSchedule(Class<? extends WaitingStoreSchedule> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.store = inits.isInitialized("store") ? new QWaitingStore(forProperty("store")) : null;
    }

}

