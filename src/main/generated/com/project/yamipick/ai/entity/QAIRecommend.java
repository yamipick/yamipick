package com.project.yamipick.ai.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;


/**
 * QAIRecommend is a Querydsl query type for AIRecommend
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QAIRecommend extends EntityPathBase<AIRecommend> {

    private static final long serialVersionUID = -286232184L;

    public static final QAIRecommend aIRecommend = new QAIRecommend("aIRecommend");

    public final DateTimePath<java.time.LocalDateTime> aiCreatedAt = createDateTime("aiCreatedAt", java.time.LocalDateTime.class);

    public final StringPath aiReason = createString("aiReason");

    public final NumberPath<Long> seqMenu = createNumber("seqMenu", Long.class);

    public final NumberPath<Long> seqRecommend = createNumber("seqRecommend", Long.class);

    public final NumberPath<Long> seqSession = createNumber("seqSession", Long.class);

    public final StringPath userInput = createString("userInput");

    public QAIRecommend(String variable) {
        super(AIRecommend.class, forVariable(variable));
    }

    public QAIRecommend(Path<? extends AIRecommend> path) {
        super(path.getType(), path.getMetadata());
    }

    public QAIRecommend(PathMetadata metadata) {
        super(AIRecommend.class, metadata);
    }

}

