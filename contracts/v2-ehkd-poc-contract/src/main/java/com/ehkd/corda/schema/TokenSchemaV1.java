package com.ehkd.corda.schema;

import net.corda.core.schemas.MappedSchema;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import java.util.ArrayList;
import java.util.Arrays;

/**
 * @author Kris Lau
 * @date 2023/3/22
 */
public class TokenSchemaV1 extends MappedSchema {
    @Nullable
    @Override
    public String getMigrationResource() {
        return "ehkd.changelog-master";
    }

    public TokenSchemaV1(@NotNull Class<?> schemaFamily, int version, @NotNull Iterable<? extends Class<?>> mappedTypes) {
        super(schemaFamily, version, mappedTypes);
    }

    public TokenSchemaV1() {
        this(TokenSchemaV1.class, 1, new ArrayList<>(Arrays.asList(PersistentToken.class)));
    }
}
