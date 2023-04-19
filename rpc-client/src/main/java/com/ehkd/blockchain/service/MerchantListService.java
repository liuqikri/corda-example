package com.ehkd.blockchain.service;

import com.ehkd.blockchain.sdk.CordaService;
import com.ehkd.blockchain.util.CordaResult;
import com.ehkd.blockchain.util.CordaResultCodeEnum;
import com.ehkd.blockchain.util.CordaResultPage;
import com.ehkd.corda.state.MerchantListState;
import liquibase.util.StringUtils;
import net.corda.core.contracts.StateAndRef;
import net.corda.core.node.services.Vault;
import net.corda.core.transactions.SignedTransaction;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class MerchantListService {

    CordaService cordaService;

    private MerchantListService() {
    }

    public MerchantListService(CordaService cordaService) {
        this.cordaService = cordaService;
    }


    //创建商户清单
    public CordaResult<String> createMerchantList(String merchantListId,List<String> merchantIds){
        if(StringUtils.isNotEmpty(merchantListId) && merchantIds.size() > 0) {
            try {
                SignedTransaction signedTransaction = cordaService.createMerchantList(merchantListId, merchantIds);
                return CordaResult.success(signedTransaction.getId().toString());
            } catch (Exception e) {
                return CordaResult.error(e.getMessage());
            }
        }else{
            return CordaResult.error("input parameter err");
        }
    }

    //查询商户清单
    public CordaResult<List<String>> getAllMerchantList() {

        try {
            Vault.Page<MerchantListState> merchantListStatePage = cordaService.searchMerchantList();
            if(merchantListStatePage != null) {
                List<StateAndRef<MerchantListState>> merchantListState = merchantListStatePage.getStates();
                if(merchantListState != null&&merchantListState.size()>0) {
                    return CordaResult.success(merchantListState.stream()
                            .map(m->m.getState().getData().getMerchantListId())
                            .collect(Collectors.toList()));
                }else{
                    return CordaResult.error("get merchant list state err");
                }
            }else{
                return CordaResult.error("get merchant list err");
            }
        }catch (Exception e){
            return CordaResult.error(e.getMessage());
        }
    }

    //查询商户清单详情
    public CordaResultPage getMerchantListById(String merchantListId, Integer pageNumber, Integer pageSize) {

        CordaResultPage merchantListDetailEntity = new CordaResultPage();
        try {
            StateAndRef<MerchantListState> merchantListGroupState = cordaService.getMerchantListById(merchantListId);
            if(merchantListGroupState != null) {
                if(pageSize == null||pageSize<0) {
                    pageSize = 20;
                }
                List<String>members = merchantListGroupState.getState().getData().getMerchantList();
                Long total = BigDecimal.valueOf(members.size())
                        .divide(BigDecimal.valueOf(pageSize), BigDecimal.ROUND_UP).longValue();
                if(pageNumber == null||pageNumber<0) {
                    pageNumber = 1;
                }
                if(pageNumber<=total) {
                    Integer start = (pageNumber-1)*pageSize;
                    Integer end = pageNumber*pageSize;
                    if(end>members.size()) {
                        end = members.size();
                    }
                    merchantListDetailEntity.setData(members.subList(start, end));
                    CordaResultPage.success(total,pageNumber,pageSize,members.subList(start, end));
                }
                merchantListDetailEntity.setPageNumber(pageNumber);
                merchantListDetailEntity.setPageSize(pageSize);
                merchantListDetailEntity.setTotalCount(total);
                merchantListDetailEntity.setCode(CordaResultCodeEnum.SUCCESS.getCode());
            } else {
                merchantListDetailEntity.setPageNumber(pageNumber);
                merchantListDetailEntity.setPageSize(pageSize);
                merchantListDetailEntity.setCode(CordaResultCodeEnum.ERROR.getCode());
            }
            return merchantListDetailEntity;
        }catch(Exception e){
            return CordaResultPage.error(e.getMessage());
        }
    }

    //更新商户清单
    public CordaResult<String> updateMerchantList(String merchantListId,List<String> merchantIds) {
        try {
            if (StringUtils.isNotEmpty(merchantListId) && merchantIds != null && merchantIds.size() > 0) {
                StateAndRef<MerchantListState> merchantListState = cordaService.getMerchantListById(merchantListId);
                if (merchantListState != null) {
                    SignedTransaction signedTransaction = cordaService.updateMerchantList(merchantListState.getRef(), merchantIds);
                    return CordaResult.success(signedTransaction.getId().toString());
                }else{
                    return CordaResult.error("search merchant list state err");
                }
            }else{
                return CordaResult.error("input parameter err");
            }
        }catch (Exception e){
            return CordaResult.error(e.getMessage());
        }

    }

    //增加商户清单成员
    public CordaResult<String> addMerchant(String merchantListId,List<String> merchantIds){
        if(StringUtils.isNotEmpty(merchantListId) && merchantIds != null && merchantIds.size()>0) {
            StateAndRef<MerchantListState> merchantListStateAndRef = cordaService.getMerchantListById(merchantListId);
            if(merchantListStateAndRef != null) {
                List<String> oldMerchantIds = merchantListStateAndRef.getState().getData().getMerchantList();
                merchantIds = Stream.of(oldMerchantIds, merchantIds).flatMap(Collection::stream)
                        .distinct()
                        .collect(Collectors.toList());
                try {
                    SignedTransaction signedTransaction = cordaService.updateMerchantList(merchantListStateAndRef.getRef(), merchantIds);
                    return CordaResult.success(signedTransaction.getId().toString());
                }catch (Exception e) {
                    return CordaResult.error(e.getMessage());
                }
            }else{
                return CordaResult.error("search merchant list state err");
            }
        }else{
            return CordaResult.error("input parameter err");
        }
    }

    //删除商户清单成员
    public CordaResult<String> deleteMerchant(String merchantListId, List<String> merchantIds){
        if(StringUtils.isNotEmpty(merchantListId) && merchantIds != null && merchantIds.size()>0) {
            StateAndRef<MerchantListState> merchantListStateAndRef = cordaService.getMerchantListById(merchantListId);
            if(merchantListStateAndRef != null) {
                List<String> oldMerchantIds = merchantListStateAndRef.getState().getData().getMerchantList();
                oldMerchantIds = new ArrayList<String>(oldMerchantIds);
                oldMerchantIds.removeAll(merchantIds);
                try {
                    SignedTransaction signedTransaction = cordaService.updateMerchantList(merchantListStateAndRef.getRef(), oldMerchantIds);
                    return CordaResult.success(signedTransaction.getId().toString());
                }catch (Exception e){
                    return CordaResult.error(e.getMessage());
                }
            }else{
                return CordaResult.error("search merchant list state err");
            }
        }else{
            return CordaResult.error("input parameter err");
        }
    }
}
