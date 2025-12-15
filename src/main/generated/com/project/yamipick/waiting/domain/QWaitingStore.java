package com.project.yamipick.waiting.domain;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;


/**
 * QWaitingStore is a Querydsl query type for WaitingStore
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QWaitingStore extends EntityPathBase<WaitingStore> {

    private static final long serialVersionUID = 1580358102L;

    public static final QWaitingStore waitingStore = new QWaitingStore("waitingStore");

    public final StringPath address = createString("address");

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final StringPath kakaoPlaceId = createString("kakaoPlaceId");

    public final StringPath name = createString("name");

    public final NumberPath<Long> ownerId = createNumber("ownerId", Long.class);

    public final StringPath phone = createString("phone");

    public QWaitingStore(String variable) {
        super(WaitingStore.class, forVariable(variable));
    }

    public QWaitingStore(Path<? extends WaitingStore> path) {
        super(path.getType(), path.getMetadata());
    }

    public QWaitingStore(PathMetadata metadata) {
        super(WaitingStore.class, metadata);
    }

}

