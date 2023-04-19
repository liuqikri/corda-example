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
@Table(name = "member_states")
public class PersistentMember extends PersistentState {

    @Column(name = "user_id")
    public String userId;

    @Column(name = "type")
    public String type;

    @Column(name = "industry")
    public String industry;

    @Column(name = "linear_id")
    @Type(type = "uuid-char")
    public UUID linearId;

    public PersistentMember(String userId, String type, String industry,UUID linearId) {
        super();
        this.userId = userId;
        this.type = type;
        this.industry = industry;
        this.linearId = linearId;
    }

    public PersistentMember() {
        this("", "", "",UUID.randomUUID());
    }
}
