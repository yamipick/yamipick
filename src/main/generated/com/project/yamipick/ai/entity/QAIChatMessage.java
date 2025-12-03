package com.project.yamipick.ai.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;


/**
 * QAIChatMessage is a Querydsl query type for AIChatMessage
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QAIChatMessage extends EntityPathBase<AIChatMessage> {

    private static final long serialVersionUID = 1296461659L;

    public static final QAIChatMessage aIChatMessage = new QAIChatMessage("aIChatMessage");

    public final DateTimePath<java.time.LocalDateTime> messageCreatedAt = createDateTime("messageCreatedAt", java.time.LocalDateTime.class);

    public final StringPath messageText = createString("messageText");

    public final StringPath senderType = createString("senderType");

    public final NumberPath<Long> seqMessage = createNumber("seqMessage", Long.class);

    public final NumberPath<Long> seqSession = createNumber("seqSession", Long.class);

    public QAIChatMessage(String variable) {
        super(AIChatMessage.class, forVariable(variable));
    }

    public QAIChatMessage(Path<? extends AIChatMessage> path) {
        super(path.getType(), path.getMetadata());
    }

    public QAIChatMessage(PathMetadata metadata) {
        super(AIChatMessage.class, metadata);
    }

}

