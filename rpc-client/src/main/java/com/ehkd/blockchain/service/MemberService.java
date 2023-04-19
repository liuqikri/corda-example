package com.ehkd.blockchain.service;

import com.ehkd.blockchain.entity.MemberEntity;
import com.ehkd.blockchain.sdk.CordaService;
import com.ehkd.blockchain.util.CordaResult;
import com.ehkd.corda.payload.Member;
import com.ehkd.corda.state.MemberState;
import net.corda.core.contracts.StateAndRef;
import net.corda.core.transactions.SignedTransaction;
import org.apache.commons.lang3.StringUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

public class MemberService {

    CordaService cordaService;

    private MemberService() {
    }

    public MemberService(CordaService cordaService) {
        this.cordaService = cordaService;
    }

    //创建机构
    public CordaResult<String> createMember(MemberEntity info){
        if(info.getUserId() == null || info.getUserId().equals("")){
            return CordaResult.error("User id can not be empty");
        }
        if(info.getType() == null || info.getType().equals("")){
            return CordaResult.error("Member type can not be empty");
        }
        if(info.getType().equalsIgnoreCase("merchant") && (info.getIndustry() == null || info.getIndustry().equals(""))){
            return CordaResult.error("Merchant member industry can not be empty");
        }
        try {
            SignedTransaction signedTransaction = cordaService.createMember(info);
            System.out.println(signedTransaction.toString());
            return CordaResult.success(signedTransaction.getId().toString());
        }catch (Exception e){
            return CordaResult.error(e.getMessage());
        }
    }

    //更新机构信息
    public CordaResult<String> UpdateMember(MemberEntity info) {
        if(info.getUserId() == null || info.getUserId().equals("")){
            return CordaResult.error("User id can not be empty");
        }
        if(info.getType() == null || info.getType().equals("")){
            return CordaResult.error("Member type can not be empty");
        }
        try {
            StateAndRef<MemberState> result = cordaService.searchMemberByUserId(info.getUserId(),MemberState.class);
            SignedTransaction signedTransaction = cordaService.UpdateMember(result.getRef(), new Member(
                    info.getUserId(),
                    info.getType(),
                    info.getName(),
                    info.getIndustry()
            ));
            return CordaResult.success(signedTransaction.getId().toString());
        }catch (Exception e){
            return CordaResult.error(e.getMessage());
        }
    }

    public CordaResult<Map<String, String>> memberToLowerCase(String userId) {
        try {
            Map<String, String> result = new HashMap<>(1);
            List<StateAndRef<MemberState>> list = cordaService.searchMemberAll(userId);
            if(list != null && list.size()>0) {
                MemberState member = null;
                SignedTransaction signedTransaction = null;
                for(StateAndRef<MemberState> l:list) {
                    member = l.getState().getData();
                    signedTransaction = cordaService.UpdateMember(l.getRef(), new Member(
                            member.getUserId(),
                            StringUtils.isNotBlank(member.getType())?member.getType().toLowerCase():member.getType(),
                            StringUtils.isNotBlank(member.getName())?member.getName().toLowerCase():member.getName(),
                            StringUtils.isNotBlank(member.getIndustry())?member.getIndustry().toLowerCase():member.getIndustry()
                    ));
                    if(signedTransaction != null) {
                        result.put(member.getUserId(), signedTransaction.getId().toString());
                    }
                }
            }
            return CordaResult.success(result);
        }catch (Exception e){
            return CordaResult.error(e.getMessage());
        }
    }


    //通过Id查询机构信息
    public MemberEntity searchMemberByUserId(String userId) throws ExecutionException, InterruptedException{
        MemberEntity memberEntity = new MemberEntity();
        StateAndRef<MemberState> result = cordaService.searchMemberByUserId(userId,MemberState.class);
        MemberState memberState = result.getState().getData();
        System.out.println(memberState.getUserId());
        memberEntity.setUserId(memberState.getUserId());
        memberEntity.setType(memberState.getType());
        memberEntity.setIndustry(memberState.getIndustry());
        return memberEntity;
    }

}
