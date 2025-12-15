package com.project.yamipick.review.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QFavoriteReview is a Querydsl query type for FavoriteReview
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QFavoriteReview extends EntityPathBase<FavoriteReview> {

    private static final long serialVersionUID = -997967024L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QFavoriteReview favoriteReview = new QFavoriteReview("favoriteReview");

    public final DateTimePath<java.sql.Timestamp> regdate = createDateTime("regdate", java.sql.Timestamp.class);

    public final QBoardReview review;

    public final NumberPath<Long> seqFavoriteReview = createNumber("seqFavoriteReview", Long.class);

    public final com.project.yamipick.user.entity.QUser user;

    public QFavoriteReview(String variable) {
        this(FavoriteReview.class, forVariable(variable), INITS);
    }

    public QFavoriteReview(Path<? extends FavoriteReview> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QFavoriteReview(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QFavoriteReview(PathMetadata metadata, PathInits inits) {
        this(FavoriteReview.class, metadata, inits);
    }

    public QFavoriteReview(Class<? extends FavoriteReview> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.review = inits.isInitialized("review") ? new QBoardReview(forProperty("review"), inits.get("review")) : null;
        this.user = inits.isInitialized("user") ? new com.project.yamipick.user.entity.QUser(forProperty("user")) : null;
    }

}

