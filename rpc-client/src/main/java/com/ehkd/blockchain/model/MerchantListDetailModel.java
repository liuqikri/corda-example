package com.ehkd.blockchain.model;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.Entity;
import java.util.List;

@Entity
@Getter
@Setter
public class MerchantListDetailModel {

    private Long totalCount;

    private Integer pageNumber;

    private Integer pageSize;

    List<String> merchantIds;
}
