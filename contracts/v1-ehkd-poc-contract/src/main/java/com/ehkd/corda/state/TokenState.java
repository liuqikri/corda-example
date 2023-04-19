package com.ehkd.corda.state;

import com.ehkd.corda.contract.TokenContract;
import com.ehkd.corda.schema.PersistentToken;
import com.ehkd.corda.schema.TokenSchemaV1;
import lombok.Getter;
import lombok.Setter;
import net.corda.core.contracts.*;
import net.corda.core.flows.FlowLogicRefFactory;
import net.corda.core.identity.AbstractParty;
import net.corda.core.identity.Party;
import net.corda.core.schemas.MappedSchema;
import net.corda.core.schemas.PersistentState;
import net.corda.core.schemas.QueryableState;
import net.corda.core.serialization.ConstructorForDeserialization;
import net.corda.core.serialization.CordaSerializable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Kris Lau
 * @date 2023/3/22
 */
@BelongsToContract(TokenContract.class)
@CordaSerializable
@Setter
@Getter
public class TokenState implements QueryableState, ContractState,SchedulableState {
    UniqueIdentifier id;
    UniqueIdentifier batchId;
    String name;
    String symbol;
    Integer decimals;
    Long amount;
    String sponsor;
    String owner;
    String issuer;
    Instant releaseDate;
    Instant expiryDate;
    String tokenType;
    String merchantList;
    String lockedBy;
    Integer isVoucher;
    List<Party> parties;

    @ConstructorForDeserialization
    public TokenState(UniqueIdentifier id,
                      UniqueIdentifier batchId,
                      String name,
                      String symbol,
                      Integer decimals,
                      Long amount,
                      String sponsor,
                      String owner,
                      String issuer,
                      Instant releaseDate,
                      Instant expiryDate,
                      String tokenType,
                      String merchantList,
                      String lockedBy,
                      Integer isVoucher,
                      List<Party> parties
) {
        this.id = id;
        this.batchId = batchId;
        this.name = name;
        this.symbol = symbol;
        this.decimals = decimals;
        this.amount = amount;
        this.sponsor = sponsor;
        this.owner = owner;
        this.issuer = issuer;
        this.releaseDate = releaseDate;
        this.expiryDate = expiryDate;
        this.tokenType = tokenType;
        this.merchantList = merchantList;
        this.lockedBy = lockedBy;
        this.isVoucher = isVoucher;
        this.parties = parties;
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
        if(schema instanceof TokenSchemaV1) {
            return new PersistentToken(id.getId(),batchId.getId(),name,amount,owner,tokenType,merchantList,isVoucher);
        } else {
            throw new IllegalArgumentException("Unrecognised schema $schema");
        }
    }

    @NotNull
    @Override
    public Iterable<MappedSchema> supportedSchemas() {
        ArrayList<MappedSchema> result = new ArrayList<>();
        result.add(new TokenSchemaV1());
        return result;
    }

    public TokenState(
                      UniqueIdentifier batchId,
                      String name,
                      String symbol,
                      Integer decimals,
                      Long amount,
                      String sponsor,
                      String owner,
                      String issuer,
                      Instant releaseDate,
                      Instant expiryDate,
                      String tokenType,
                      String merchantList,
                      String lockedBy,
                      Integer isVoucher,
                      List<Party> parties) {
        this.id = new UniqueIdentifier();
        this.batchId = batchId;
        this.name = name;
        this.symbol = symbol;
        this.decimals = decimals;
        this.amount = amount;
        this.sponsor = sponsor;
        this.owner = owner;
        this.issuer = issuer;
        this.releaseDate = releaseDate;
        this.expiryDate = expiryDate;
        this.tokenType = tokenType;
        this.isVoucher = isVoucher;
        this.merchantList = merchantList;
        this.lockedBy = lockedBy;
        this.parties = parties;
    }

    public TokenState deepCopy() {
        return new TokenState(
                id,
                batchId,
                name,
                symbol,
                decimals,
                amount,
                sponsor,
                owner,
                issuer,
                releaseDate,
                expiryDate,
                tokenType,
                merchantList,
                lockedBy,
                isVoucher,
                parties
        );
    }

    @Nullable
    @Override
    public ScheduledActivity nextScheduledActivity(@NotNull StateRef thisStateRef, @NotNull FlowLogicRefFactory flowLogicRefFactory) {

        if(expiryDate != null){
            return new ScheduledActivity(flowLogicRefFactory.create("com.ehkd.corda.flow.RedeemFlow", thisStateRef),expiryDate );
        }else{
            return null;
        }

    }

}
