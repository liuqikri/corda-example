package com.ehkd.blockchain.model;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.Entity;
import java.util.List;

@Entity
@Getter
@Setter
public class MerchantListModel {

    String merchantListId;
    String merchantListName;
    List<String> merchantIds;
}
