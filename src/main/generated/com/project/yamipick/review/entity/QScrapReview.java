package com.project.yamipick.review.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QScrapReview is a Querydsl query type for ScrapReview
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QScrapReview extends EntityPathBase<ScrapReview> {

    private static final long serialVersionUID = -1676525363L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QScrapReview scrapReview = new QScrapReview("scrapReview");

    public final DateTimePath<java.sql.Timestamp> regdate = createDateTime("regdate", java.sql.Timestamp.class);

    public final QBoardReview review;

    public final NumberPath<Long> seqscrapReview = createNumber("seqscrapReview", Long.class);

    public final com.project.yamipick.user.entity.QUser user;

    public QScrapReview(String variable) {
        this(ScrapReview.class, forVariable(variable), INITS);
    }

    public QScrapReview(Path<? extends ScrapReview> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QScrapReview(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QScrapReview(PathMetadata metadata, PathInits inits) {
        this(ScrapReview.class, metadata, inits);
    }

    public QScrapReview(Class<? extends ScrapReview> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.review = inits.isInitialized("review") ? new QBoardReview(forProperty("review"), inits.get("review")) : null;
        this.user = inits.isInitialized("user") ? new com.project.yamipick.user.entity.QUser(forProperty("user")) : null;
    }

}

