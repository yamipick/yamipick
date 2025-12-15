package com.project.yamipick.waiting.domain;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;


/**
 * QWaitingStatus is a Querydsl query type for WaitingStatus
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QWaitingStatus extends EntityPathBase<WaitingStatus> {

    private static final long serialVersionUID = 1746046365L;

    public static final QWaitingStatus waitingStatus = new QWaitingStatus("waitingStatus");

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final StringPath statusName = createString("statusName");

    public QWaitingStatus(String variable) {
        super(WaitingStatus.class, forVariable(variable));
    }

    public QWaitingStatus(Path<? extends WaitingStatus> path) {
        super(path.getType(), path.getMetadata());
    }

    public QWaitingStatus(PathMetadata metadata) {
        super(WaitingStatus.class, metadata);
    }

}

