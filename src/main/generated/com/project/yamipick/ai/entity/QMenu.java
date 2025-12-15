package com.project.yamipick.ai.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;


/**
 * QMenu is a Querydsl query type for Menu
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QMenu extends EntityPathBase<Menu> {

    private static final long serialVersionUID = -1616536597L;

    public static final QMenu menu = new QMenu("menu");

    public final StringPath flavorTags = createString("flavorTags");

    public final StringPath menuCategory = createString("menuCategory");

    public final StringPath menuDescription = createString("menuDescription");

    public final StringPath menuEnglish = createString("menuEnglish");

    public final StringPath menuName = createString("menuName");

    public final NumberPath<Long> seqMenu = createNumber("seqMenu", Long.class);

    public QMenu(String variable) {
        super(Menu.class, forVariable(variable));
    }

    public QMenu(Path<? extends Menu> path) {
        super(path.getType(), path.getMetadata());
    }

    public QMenu(PathMetadata metadata) {
        super(Menu.class, metadata);
    }

}

