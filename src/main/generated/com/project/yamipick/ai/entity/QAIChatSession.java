package com.project.yamipick.ai.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;


/**
 * QAIChatSession is a Querydsl query type for AIChatSession
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QAIChatSession extends EntityPathBase<AIChatSession> {

    private static final long serialVersionUID = -1968442902L;

    public static final QAIChatSession aIChatSession = new QAIChatSession("aIChatSession");

    public final NumberPath<Long> seqSession = createNumber("seqSession", Long.class);

    public final NumberPath<Long> seqUser = createNumber("seqUser", Long.class);

    public final DateTimePath<java.time.LocalDateTime> sessionCreatedAt = createDateTime("sessionCreatedAt", java.time.LocalDateTime.class);

    public QAIChatSession(String variable) {
        super(AIChatSession.class, forVariable(variable));
    }

    public QAIChatSession(Path<? extends AIChatSession> path) {
        super(path.getType(), path.getMetadata());
    }

    public QAIChatSession(PathMetadata metadata) {
        super(AIChatSession.class, metadata);
    }

}

