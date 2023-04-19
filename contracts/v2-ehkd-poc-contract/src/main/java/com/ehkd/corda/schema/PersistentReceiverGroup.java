package com.ehkd.corda.schema;

import net.corda.core.schemas.PersistentState;
import org.hibernate.annotations.Type;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * @author Kris Lau
 * @date 2023/3/22
 */
@Entity
@Table(name = "receiver_group_states")
public class PersistentReceiverGroup extends PersistentState {

    @Column(name = "group_id")
    public String groupId;


    @Column(name = "linear_id")
    @Type(type = "uuid-char")
    public UUID linearId;

    public PersistentReceiverGroup(String groupId, UUID linearId) {
        super();
        this.groupId = groupId;
        this.linearId = linearId;
    }

    public PersistentReceiverGroup() {
        this("", UUID.randomUUID());
    }
}
