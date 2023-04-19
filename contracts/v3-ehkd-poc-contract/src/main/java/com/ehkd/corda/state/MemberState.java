package com.ehkd.corda.state;

import com.ehkd.corda.contract.MemberContract;
import com.ehkd.corda.schema.MemberSchemaV1;
import com.ehkd.corda.schema.PersistentMember;
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
@BelongsToContract(MemberContract.class)
@CordaSerializable
@Setter
@Getter
public class MemberState implements LinearState, QueryableState {
    String userId;
    String type;
    String industry;

    String name;
    List<Party> parties;
    UniqueIdentifier linearId;

    @ConstructorForDeserialization
    public MemberState(String userId, String type, String name, String industry, List<Party> parties,UniqueIdentifier linearId) {
        this.userId = userId;
        this.type = type;
        this.name = name;
        this.industry = industry;
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
        if(schema instanceof MemberSchemaV1) {
            return new PersistentMember(userId,type,name,industry,linearId.getId());
        } else {
            throw new IllegalArgumentException("Unrecognised schema $schema");
        }
    }

    @NotNull
    @Override
    public Iterable<MappedSchema> supportedSchemas() {
        ArrayList<MappedSchema> result = new ArrayList<>();
        result.add(new MemberSchemaV1());
        return result;
    }

    public MemberState(String userId,
                   String type,
                   String name,
                   String industry,
                   List<Party> parties) {
        this.userId = userId;
        this.type = type;
        this.name = name;
        this.industry = industry;
        this.parties = parties;
        this.linearId = new UniqueIdentifier();
    }

    public MemberState deepCopy() {
        return new MemberState(
                userId,
                type,
                name,
                industry,
                parties,
                linearId
        );
    }
}
