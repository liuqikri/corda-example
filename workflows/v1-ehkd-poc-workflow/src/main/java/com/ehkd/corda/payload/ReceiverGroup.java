package com.ehkd.corda.payload;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import net.corda.core.serialization.CordaSerializable;

import java.util.List;

/**
 * @author Kris Lau
 * @date 2023/3/22
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@CordaSerializable
public class ReceiverGroup {

    String groupId;

    List<String> members;

}
