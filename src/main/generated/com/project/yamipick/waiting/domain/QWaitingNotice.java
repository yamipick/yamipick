package com.project.yamipick.waiting.domain;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QWaitingNotice is a Querydsl query type for WaitingNotice
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QWaitingNotice extends EntityPathBase<WaitingNotice> {

    private static final long serialVersionUID = 1598837891L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QWaitingNotice waitingNotice = new QWaitingNotice("waitingNotice");

    public final StringPath content = createString("content");

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final StringPath isPinned = createString("isPinned");

    public final DateTimePath<java.time.LocalDateTime> regDate = createDateTime("regDate", java.time.LocalDateTime.class);

    public final QWaitingStore store;

    public final StringPath title = createString("title");

    public QWaitingNotice(String variable) {
        this(WaitingNotice.class, forVariable(variable), INITS);
    }

    public QWaitingNotice(Path<? extends WaitingNotice> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QWaitingNotice(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QWaitingNotice(PathMetadata metadata, PathInits inits) {
        this(WaitingNotice.class, metadata, inits);
    }

    public QWaitingNotice(Class<? extends WaitingNotice> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.store = inits.isInitialized("store") ? new QWaitingStore(forProperty("store")) : null;
    }

}

