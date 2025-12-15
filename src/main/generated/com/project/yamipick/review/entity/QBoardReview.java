package com.project.yamipick.review.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QBoardReview is a Querydsl query type for BoardReview
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QBoardReview extends EntityPathBase<BoardReview> {

    private static final long serialVersionUID = -1091468254L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QBoardReview boardReview = new QBoardReview("boardReview");

    public final StringPath attach = createString("attach");

    public final StringPath contentState = createString("contentState");

    public final NumberPath<Integer> favoriteCount = createNumber("favoriteCount", Integer.class);

    public final StringPath place = createString("place");

    public final NumberPath<Integer> readCount = createNumber("readCount", Integer.class);

    public final DateTimePath<java.sql.Timestamp> regdate = createDateTime("regdate", java.sql.Timestamp.class);

    public final StringPath reviewContent = createString("reviewContent");

    public final NumberPath<Long> seqReview = createNumber("seqReview", Long.class);

    public final NumberPath<Integer> starRating = createNumber("starRating", Integer.class);

    public final StringPath state = createString("state");

    public final com.project.yamipick.store.entity.QStore store;

    public final ListPath<Tagging, QTagging> taggings = this.<Tagging, QTagging>createList("taggings", Tagging.class, QTagging.class, PathInits.DIRECT2);

    public final StringPath title = createString("title");

    public final com.project.yamipick.user.entity.QUser user;

    public QBoardReview(String variable) {
        this(BoardReview.class, forVariable(variable), INITS);
    }

    public QBoardReview(Path<? extends BoardReview> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QBoardReview(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QBoardReview(PathMetadata metadata, PathInits inits) {
        this(BoardReview.class, metadata, inits);
    }

    public QBoardReview(Class<? extends BoardReview> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.store = inits.isInitialized("store") ? new com.project.yamipick.store.entity.QStore(forProperty("store"), inits.get("store")) : null;
        this.user = inits.isInitialized("user") ? new com.project.yamipick.user.entity.QUser(forProperty("user")) : null;
    }

}

