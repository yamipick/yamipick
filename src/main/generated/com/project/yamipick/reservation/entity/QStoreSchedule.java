package com.project.yamipick.reservation.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QStoreSchedule is a Querydsl query type for StoreSchedule
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QStoreSchedule extends EntityPathBase<StoreSchedule> {

    private static final long serialVersionUID = -985603882L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QStoreSchedule storeSchedule = new QStoreSchedule("storeSchedule");

    public final StringPath breakEnd = createString("breakEnd");

    public final StringPath breakStart = createString("breakStart");

    public final StringPath closeTime = createString("closeTime");

    public final NumberPath<Integer> dayOfWeek = createNumber("dayOfWeek", Integer.class);

    public final StringPath isOpen = createString("isOpen");

    public final StringPath openTime = createString("openTime");

    public final NumberPath<Long> seqSchedule = createNumber("seqSchedule", Long.class);

    public final com.project.yamipick.store.entity.QStore store;

    public QStoreSchedule(String variable) {
        this(StoreSchedule.class, forVariable(variable), INITS);
    }

    public QStoreSchedule(Path<? extends StoreSchedule> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QStoreSchedule(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QStoreSchedule(PathMetadata metadata, PathInits inits) {
        this(StoreSchedule.class, metadata, inits);
    }

    public QStoreSchedule(Class<? extends StoreSchedule> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.store = inits.isInitialized("store") ? new com.project.yamipick.store.entity.QStore(forProperty("store"), inits.get("store")) : null;
    }

}

