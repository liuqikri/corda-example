package com.ehkd.blockchain.service;

import com.ehkd.blockchain.sdk.CordaService;
import com.ehkd.blockchain.util.CordaResult;
import com.ehkd.blockchain.util.CordaResultCodeEnum;
import com.ehkd.blockchain.util.CordaResultPage;
import com.ehkd.corda.state.ReceiverGroupState;
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

public class ReceiverGroupService {

    private ReceiverGroupService() {
    }
    public ReceiverGroupService(CordaService cordaService) {
        this.cordaService = cordaService;
    }

    CordaService cordaService;
    //创建接收者清单
    public CordaResult<String> createReceiverGroup(String groupId,List<String> members) {
        if(StringUtils.isNotEmpty(groupId) && members.size() > 0) {
            try {
                SignedTransaction signedTransaction = cordaService.createReceiverGroup(groupId, members);
                return CordaResult.success(signedTransaction.toString());
            }catch (Exception e){
                return CordaResult.error(e.getMessage());
            }
        }else{
            return CordaResult.error("input parameter err");
        }

    }

    //查询接收者清单
    public CordaResult<List<String>> searchReceiverGroup() {
        try {
            Vault.Page<ReceiverGroupState> receivers = cordaService.searchReceiverGroup();
            if (receivers != null) {
                List<StateAndRef<ReceiverGroupState>> receiverState = receivers.getStates();
                if (receiverState != null && receiverState.size() > 0) {
                    return CordaResult.success(receiverState.stream()
                            .map(m -> m.getState().getData().getGroupId())
                            .collect(Collectors.toList()));
                }else{
                    return CordaResult.error("search receiver group state err");
                }
            } else {
                return CordaResult.error("search receiver group err");
            }
        }catch (Exception e){
            return CordaResult.error(e.getMessage());
        }
    }

    //查询接收者清单详情
    public CordaResultPage getReceiverListById(String groupId, Integer pageNumber, Integer pageSize) throws Exception{
        CordaResultPage receiverDetailEntity = new CordaResultPage();
        StateAndRef<ReceiverGroupState> receiverGroupState = cordaService.getReceiverListById(groupId);
        if(receiverGroupState != null) {
            if(pageSize == null||pageSize<0) {
                pageSize = 20;
            }
            List<String>members = receiverGroupState.getState().getData().getMembers();
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
                receiverDetailEntity.setData(members.subList(start, end));
            }
            receiverDetailEntity.setPageNumber(pageNumber);
            receiverDetailEntity.setPageSize(pageSize);
            receiverDetailEntity.setTotalCount(total);
            receiverDetailEntity.setCode(CordaResultCodeEnum.SUCCESS.getCode());
        } else {
            receiverDetailEntity.setPageNumber(pageNumber);
            receiverDetailEntity.setPageSize(pageSize);
            receiverDetailEntity.setCode(CordaResultCodeEnum.ERROR.getCode());
        }
        return receiverDetailEntity;
    }

    //更新接收者清单
    public CordaResult<String> updateReceiverList(String groupId,List<String> members) throws Exception{
        if(StringUtils.isNotEmpty(groupId) && members != null && members.size()>0) {
            StateAndRef<ReceiverGroupState> receiverGroupState = cordaService.getReceiverListById(groupId);
            if(receiverGroupState != null) {
                SignedTransaction signedTransaction = cordaService.updateReceiverGroup(receiverGroupState.getRef(), members);
                return CordaResult.success(signedTransaction.getId().toString());
            }else{
                return CordaResult.error("update receiver group err");
            }
        }else{
            return CordaResult.error("input parameter err");
        }
    }

    //增加接收者清单成员
    public CordaResult<String> addMember(String groupId,List<String> members) throws Exception{
        if(StringUtils.isNotEmpty(groupId) && members != null && members.size()>0) {
            StateAndRef<ReceiverGroupState> receiverGroupState = cordaService.getReceiverListById(groupId);
            if(receiverGroupState != null) {
                List<String> oldMembers = receiverGroupState.getState().getData().getMembers();
                members = Stream.of(oldMembers, members).flatMap(Collection::stream)
                        .distinct()
                        .collect(Collectors.toList());
                SignedTransaction signedTransaction = cordaService.updateReceiverGroup(receiverGroupState.getRef(), members);
                return CordaResult.success(signedTransaction.getId().toString());
            }else{
                return CordaResult.error("add member into receiver group err");
            }
        }else{
            return CordaResult.error("input parameter err");
        }
    }

    //删除接收者清单成员
    public CordaResult<String> deleteMember(String groupId,List<String> members) throws Exception {
        if (StringUtils.isNotEmpty(groupId) && members != null && members.size() > 0) {
            StateAndRef<ReceiverGroupState> receiverGroupState = cordaService.getReceiverListById(groupId);
            if (receiverGroupState != null) {
                List<String> oldMembers = receiverGroupState.getState().getData().getMembers();
                oldMembers = new ArrayList<String>(oldMembers);
                oldMembers.removeAll(members);
                SignedTransaction signedTransaction = cordaService.updateReceiverGroup(receiverGroupState.getRef(), oldMembers);
                return CordaResult.success(signedTransaction.getId().toString());
            } else {
                return CordaResult.error("delete member from receiver group err");
            }
        } else {
            return CordaResult.error("input parameter err");
        }
    }
}
