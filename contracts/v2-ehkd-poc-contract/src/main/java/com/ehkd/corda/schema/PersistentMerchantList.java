package com.ehkd.corda.schema;

import net.corda.core.schemas.PersistentState;
import org.hibernate.annotations.Type;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import java.util.UUID;

/**
 * @author Kris Lau
 * @date 2023/3/22
 */
@Entity
@Table(name = "merchant_list_states")
public class PersistentMerchantList extends PersistentState {

    @Column(name = "merchant_list_id")
    public String merchantListId;

    @Column(name = "merchant_list")
    public String merchantList;

    @Column(name = "linear_id")
    @Type(type = "uuid-char")
    public UUID linearId;

    public PersistentMerchantList(String merchantListId, String merchantList, UUID linearId) {
        super();
        this.merchantListId = merchantListId;
        this.merchantList = merchantList;
        this.linearId = linearId;
    }

    public PersistentMerchantList() {
        this("", null, UUID.randomUUID());
    }
}
