package com.project.yamipick.reservation.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QStoreTableType is a Querydsl query type for StoreTableType
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QStoreTableType extends EntityPathBase<StoreTableType> {

    private static final long serialVersionUID = 1978212809L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QStoreTableType storeTableType = new QStoreTableType("storeTableType");

    public final NumberPath<Integer> capacity = createNumber("capacity", Integer.class);

    public final StringPath name = createString("name");

    public final NumberPath<Integer> quantity = createNumber("quantity", Integer.class);

    public final NumberPath<Long> seqStoreTable = createNumber("seqStoreTable", Long.class);

    public final com.project.yamipick.store.entity.QStore store;

    public QStoreTableType(String variable) {
        this(StoreTableType.class, forVariable(variable), INITS);
    }

    public QStoreTableType(Path<? extends StoreTableType> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QStoreTableType(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QStoreTableType(PathMetadata metadata, PathInits inits) {
        this(StoreTableType.class, metadata, inits);
    }

    public QStoreTableType(Class<? extends StoreTableType> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.store = inits.isInitialized("store") ? new com.project.yamipick.store.entity.QStore(forProperty("store"), inits.get("store")) : null;
    }

}

