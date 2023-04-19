package com.ehkd.blockchain;

import com.ehkd.blockchain.entity.MemberEntity;
import com.ehkd.blockchain.sdk.CordaConfig;
import com.ehkd.blockchain.sdk.CordaService;
import com.ehkd.blockchain.service.MemberService;
import com.ehkd.blockchain.util.CordaResult;
import com.ehkd.corda.payload.Member;
import com.ehkd.corda.state.MemberState;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import net.corda.core.contracts.StateAndRef;
import net.corda.core.transactions.SignedTransaction;
import org.junit.Test;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

/**
 * @author Kris Lau
 * @date 2023/3/22
 */
public class MemberTest {

//    CordaService cordaService = new CordaService(new CordaConfig());
    CordaService cordaService = new CordaService("127.0.0.1","10006");

    @Test
    public void crateMember() throws Exception {
        MemberEntity memberEntity = new MemberEntity();
        memberEntity.setUserId("555");
        memberEntity.setType("user");
        memberEntity.setName("liuqi");
        memberEntity.setIndustry("shopping");
        
        SignedTransaction signedTransaction = cordaService.createMember(memberEntity);
        System.out.println(signedTransaction.toString());
    }

    @Test
    public void searchMemberByUserId(){
        String userId = "123";
        
        StateAndRef<MemberState> result = cordaService.searchMemberByUserId(userId,MemberState.class);
        MemberState memberState = result.getState().getData();
        System.out.println(memberState.getUserId());
    }

    @Test
    public void updateMemberByUserId() throws ExecutionException, InterruptedException {
//        String userId = "test67";
//1,2,3
//        1 = List 1 (a,b,c)
//        2 = List 2 (a,d,f)
//        3 = List 3 (d,c,g)
        StateAndRef<MemberState> result = cordaService.searchMemberByUserId("test67",MemberState.class);
        cordaService.UpdateMember(result.getRef(), new Member(
                "123456789",
                "man1",
                "kris",
                "blockchain1"
        ));

    }

    @Test
    public void memberToLowerCase() throws JsonProcessingException {
        MemberService memberService = new MemberService(cordaService);
        CordaResult<Map<String, String>> data = memberService.memberToLowerCase(null);
        System.out.println("memberToLowerCase 返回结果：+++++++++++++++++++++++++++++++++++++++");
        if(data==null) {
            System.out.println("[]");
        } else {
            ObjectMapper objectMapper = new ObjectMapper();
            objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
            System.out.println(objectMapper.writeValueAsString(data));
        }
    }

}
