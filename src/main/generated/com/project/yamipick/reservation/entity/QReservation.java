package com.project.yamipick.reservation.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QReservation is a Querydsl query type for Reservation
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QReservation extends EntityPathBase<Reservation> {

    private static final long serialVersionUID = -1303158L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QReservation reservation = new QReservation("reservation");

    public final StringPath cancelReason = createString("cancelReason");

    public final NumberPath<Integer> peopleCount = createNumber("peopleCount", Integer.class);

    public final DatePath<java.time.LocalDate> reserveDate = createDate("reserveDate", java.time.LocalDate.class);

    public final StringPath reserveTime = createString("reserveTime");

    public final NumberPath<Long> seqReservation = createNumber("seqReservation", Long.class);

    public final StringPath status = createString("status");

    public final com.project.yamipick.store.entity.QStore store;

    public final QStoreTableType storeTableType;

    public final com.project.yamipick.user.entity.QUser user;

    public QReservation(String variable) {
        this(Reservation.class, forVariable(variable), INITS);
    }

    public QReservation(Path<? extends Reservation> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QReservation(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QReservation(PathMetadata metadata, PathInits inits) {
        this(Reservation.class, metadata, inits);
    }

    public QReservation(Class<? extends Reservation> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.store = inits.isInitialized("store") ? new com.project.yamipick.store.entity.QStore(forProperty("store"), inits.get("store")) : null;
        this.storeTableType = inits.isInitialized("storeTableType") ? new QStoreTableType(forProperty("storeTableType"), inits.get("storeTableType")) : null;
        this.user = inits.isInitialized("user") ? new com.project.yamipick.user.entity.QUser(forProperty("user")) : null;
    }

}

