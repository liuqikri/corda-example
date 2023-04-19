package com.ehkd.corda.schema;

import net.corda.core.node.services.Vault;
import net.corda.core.node.services.vault.Builder;
import net.corda.core.node.services.vault.QueryCriteria;

/**
 * @author Kris Lau
 * @date 2023/3/22
 */
public class MemberContractCriteria {

    public QueryCriteria getUserIdEqualCriteria(String value, Vault.StateStatus status) {
        try {
            return new QueryCriteria.VaultCustomQueryCriteria(Builder.equal(
                    PersistentMember.class.getField("userId"), value), status);
        } catch (NoSuchFieldException e) {
            return null;
        }
    }
    public QueryCriteria getUserIdEqualCriteria(String value) {
        return getUserIdEqualCriteria(value, Vault.StateStatus.UNCONSUMED);
    }


}
