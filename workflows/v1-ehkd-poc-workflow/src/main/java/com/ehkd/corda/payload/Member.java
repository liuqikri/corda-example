package com.ehkd.corda.payload;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import net.corda.core.serialization.CordaSerializable;

/**
 * @author Kris Lau
 * @date 2023/3/22
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@CordaSerializable
public class Member {

    String userId;

    String type;

    String industry;
}
