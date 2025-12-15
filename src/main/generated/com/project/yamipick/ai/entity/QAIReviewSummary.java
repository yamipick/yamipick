package com.project.yamipick.ai.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;


/**
 * QAIReviewSummary is a Querydsl query type for AIReviewSummary
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QAIReviewSummary extends EntityPathBase<AIReviewSummary> {

    private static final long serialVersionUID = 1260974074L;

    public static final QAIReviewSummary aIReviewSummary = new QAIReviewSummary("aIReviewSummary");

    public final StringPath restaurantId = createString("restaurantId");

    public final NumberPath<Long> seqSummary = createNumber("seqSummary", Long.class);

    public final DateTimePath<java.time.LocalDateTime> summaryCreatedAt = createDateTime("summaryCreatedAt", java.time.LocalDateTime.class);

    public final StringPath summaryMember = createString("summaryMember");

    public final StringPath summaryPublic = createString("summaryPublic");

    public final DateTimePath<java.time.LocalDateTime> summaryUpdatedAt = createDateTime("summaryUpdatedAt", java.time.LocalDateTime.class);

    public QAIReviewSummary(String variable) {
        super(AIReviewSummary.class, forVariable(variable));
    }

    public QAIReviewSummary(Path<? extends AIReviewSummary> path) {
        super(path.getType(), path.getMetadata());
    }

    public QAIReviewSummary(PathMetadata metadata) {
        super(AIReviewSummary.class, metadata);
    }

}

