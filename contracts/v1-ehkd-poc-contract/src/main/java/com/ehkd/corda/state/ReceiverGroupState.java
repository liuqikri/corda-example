package com.ehkd.corda.state;

import com.ehkd.corda.contract.ReceiverGroupContract;
import com.ehkd.corda.schema.PersistentReceiverGroup;
import com.ehkd.corda.schema.ReceiverGroupSchemaV1;
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
@BelongsToContract(ReceiverGroupContract.class)
@CordaSerializable
@Setter
@Getter
public class ReceiverGroupState implements LinearState, QueryableState {
    String groupId;
    List<String> members;
    List<Party> parties;
    UniqueIdentifier linearId;

    @ConstructorForDeserialization
    public ReceiverGroupState(String groupId, List<String> members, List<Party> parties, UniqueIdentifier linearId) {
        this.groupId = groupId;
        this.members = members;
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
        if(schema instanceof ReceiverGroupSchemaV1) {
            return new PersistentReceiverGroup(groupId,linearId.getId());
        } else {
            throw new IllegalArgumentException("Unrecognised schema $schema");
        }
    }

    @NotNull
    @Override
    public Iterable<MappedSchema> supportedSchemas() {
        ArrayList<MappedSchema> result = new ArrayList<>();
        result.add(new ReceiverGroupSchemaV1());
        return result;
    }

    public ReceiverGroupState(String groupId,
                              List<String> members,
                              List<Party> parties) {
        this.groupId = groupId;
        this.members = members;
        this.parties = parties;
        this.linearId = new UniqueIdentifier();
    }

    public ReceiverGroupState deepCopy() {
        return new ReceiverGroupState(
                groupId,
                members,
                parties,
                linearId
        );
    }
}
