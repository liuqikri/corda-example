package com.ehkd.corda.state;

import com.ehkd.corda.contract.MerchantListContract;
import com.ehkd.corda.schema.MerchantListSchemaV1;
import com.ehkd.corda.schema.PersistentMerchantList;
import liquibase.util.StringUtils;
import lombok.Getter;
import lombok.Setter;
import net.corda.core.contracts.BelongsToContract;
import net.corda.core.contracts.LinearState;
import net.corda.core.contracts.UniqueIdentifier;
import net.corda.core.identity.AbstractParty;
import net.corda.core.identity.Party;
import net.corda.core.schemas.MappedSchema;
import net.corda.core.schemas.PersistentState;
import net.corda.core.schemas.QueryableState;
import net.corda.core.serialization.ConstructorForDeserialization;
import net.corda.core.serialization.CordaSerializable;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Kris Lau
 * @date 2023/3/22
 */
@BelongsToContract(MerchantListContract.class)
@CordaSerializable
@Setter
@Getter
public class MerchantListState implements LinearState, QueryableState {
    String merchantListId;
    List<String> merchantList;
    List<Party> parties;
    UniqueIdentifier linearId;

    @ConstructorForDeserialization
    public MerchantListState(String merchantListId, List<String> merchantList, List<Party> parties, UniqueIdentifier linearId) {
        this.merchantListId = merchantListId;
        this.merchantList = merchantList;
        this.parties = parties;
        this.linearId = linearId;
    }

    @NotNull
    @Override
    public List<AbstractParty> getParticipants() {
        ArrayList<AbstractParty> result = new ArrayList<>();
        result.addAll(parties);
        return result;
    }


    @NotNull
    @Override
    public PersistentState generateMappedObject(@NotNull MappedSchema schema) {
        if(schema instanceof MerchantListSchemaV1) {
            return new PersistentMerchantList(merchantListId, merchantList!=null? StringUtils.join(merchantList,","):"",linearId.getId());
        } else {
            throw new IllegalArgumentException("Unrecognised schema $schema");
        }
    }

    @NotNull
    @Override
    public Iterable<MappedSchema> supportedSchemas() {
        ArrayList<MappedSchema> result = new ArrayList<>();
        result.add(new MerchantListSchemaV1());
        return result;
    }

    public MerchantListState(String merchantListId,
                             List<String> merchantList,
                             List<Party> parties) {
        this.merchantListId = merchantListId;
        this.merchantList = merchantList;
        this.parties = parties;
        this.linearId = new UniqueIdentifier();
    }

    public MerchantListState deepCopy() {
        return new MerchantListState(
                merchantListId,
                merchantList,
                parties,
                linearId
        );
    }
}
