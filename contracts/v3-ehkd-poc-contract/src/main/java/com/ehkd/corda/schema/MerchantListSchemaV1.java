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
public class MerchantListSchemaV1 extends MappedSchema {
    @Nullable
    @Override
    public String getMigrationResource() {
        return "ehkd.changelog-master";
    }

    public MerchantListSchemaV1(@NotNull Class<?> schemaFamily, int version, @NotNull Iterable<? extends Class<?>> mappedTypes) {
        super(schemaFamily, version, mappedTypes);
    }

    public MerchantListSchemaV1() {
        this(MerchantListSchemaV1.class, 1, new ArrayList<>(Arrays.asList(PersistentMerchantList.class)));
    }
}
