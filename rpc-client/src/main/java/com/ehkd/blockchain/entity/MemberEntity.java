package com.ehkd.blockchain.entity;

import lombok.*;
import net.corda.core.serialization.CordaSerializable;

@Getter
@Setter
@CordaSerializable
@AllArgsConstructor
@NoArgsConstructor
public class MemberEntity {

    String userId;
    String type;
    String name;
    String industry;
}
