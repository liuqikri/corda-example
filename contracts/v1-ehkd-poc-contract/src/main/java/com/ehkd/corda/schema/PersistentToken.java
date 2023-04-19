package com.ehkd.corda.schema;

import net.corda.core.contracts.UniqueIdentifier;
import net.corda.core.schemas.PersistentState;
import org.hibernate.annotations.Type;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import java.time.Instant;
import java.util.Date;
import java.util.UUID;

/**
 * @author Kris Lau
 * @date 2023/3/22
 */
@Entity
@Table(name = "token_states")
public class PersistentToken extends PersistentState {

    @Column(name = "id")
    @Type(type = "uuid-char")
    public UUID id;

    @Column(name = "batch_id")
    @Type(type = "uuid-char")
    public UUID batchId;

    @Column(name = "name")
    public String name;


    @Column(name = "amount")
    public Long amount;

    @Column(name = "owner")
    public String owner;


    @Column(name = "token_type")
    public String tokenType;

    @Column(name = "merchant_list")
    public String merchantList;

    @Column(name = "is_voucher")
    public Integer isVoucher;


    public PersistentToken( UUID id,
                            UUID batchId,
                            String name,
                            Long amount,
                            String owner,
                            String tokenType,
                            String merchantList,
                            Integer isVoucher)
    {
        super();
        this.id = id;
        this.batchId = batchId;
        this.name = name;
        this.amount = amount;
        this.owner = owner;
        this.tokenType = tokenType;
        this.merchantList = merchantList;
        this.isVoucher = isVoucher;
    }
    public PersistentToken() {
        this(
                UUID.randomUUID(),
                UUID.randomUUID(),
                "",
                0L,
                "",
                "",
                "",
                0);
    }
}
