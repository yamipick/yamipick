package com.project.yamipick.review.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QTagging is a Querydsl query type for Tagging
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QTagging extends EntityPathBase<Tagging> {

    private static final long serialVersionUID = 675841273L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QTagging tagging = new QTagging("tagging");

    public final QHashtag hashtag;

    public final QBoardReview review;

    public final NumberPath<Long> seqTagging = createNumber("seqTagging", Long.class);

    public QTagging(String variable) {
        this(Tagging.class, forVariable(variable), INITS);
    }

    public QTagging(Path<? extends Tagging> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QTagging(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QTagging(PathMetadata metadata, PathInits inits) {
        this(Tagging.class, metadata, inits);
    }

    public QTagging(Class<? extends Tagging> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.hashtag = inits.isInitialized("hashtag") ? new QHashtag(forProperty("hashtag")) : null;
        this.review = inits.isInitialized("review") ? new QBoardReview(forProperty("review"), inits.get("review")) : null;
    }

}

