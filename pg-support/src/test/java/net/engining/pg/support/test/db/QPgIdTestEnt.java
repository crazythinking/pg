package net.engining.pg.support.test.db;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.Generated;
import com.querydsl.core.types.Path;


/**
 * QPgIdTest is a Querydsl query type for PgIdTest
 */
@Generated("com.querydsl.codegen.EntitySerializer")
public class QPgIdTestEnt extends EntityPathBase<PgIdTestEnt> {

    private static final long serialVersionUID = -799052121L;

    public static final QPgIdTestEnt pgIdTest = new QPgIdTestEnt("pgIdTest");

    public final StringPath batchNumber = createString("batchNumber");

    public final StringPath snowFlakeId = createString("snowFlakeId");

    public QPgIdTestEnt(String variable) {
        super(PgIdTestEnt.class, forVariable(variable));
    }

    public QPgIdTestEnt(Path<? extends PgIdTestEnt> path) {
        super(path.getType(), path.getMetadata());
    }

    public QPgIdTestEnt(PathMetadata metadata) {
        super(PgIdTestEnt.class, metadata);
    }

}

