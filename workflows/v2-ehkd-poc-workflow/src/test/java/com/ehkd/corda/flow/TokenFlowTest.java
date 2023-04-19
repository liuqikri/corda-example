package com.ehkd.corda.flow;

import com.ehkd.corda.payload.*;
import com.ehkd.corda.state.MemberState;
import com.ehkd.corda.state.MerchantListState;
import com.ehkd.corda.state.ReceiverGroupState;
import com.ehkd.corda.state.TokenState;
import com.ehkd.corda.util.TestParties;
import com.ehkd.corda.util.TestUtil;
import liquibase.util.StringUtils;
import net.corda.core.contracts.StateAndRef;
import net.corda.core.flows.FlowException;
import org.junit.Assert;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

/**
 * @author Kris Lau
 * @date 2023/3/22
 */
public class TokenFlowTest {

    TestParties tn = new TestParties();

    @BeforeEach
    public void setup() {
        tn.setup();
    }

    @AfterEach
    public void tearDown() {
        tn.tearDown();
    }

    @Test
    public void createTokenByUserFlow() throws ExecutionException, InterruptedException {
        Long amount = 1099L;
        Token token = new Token(
                "1",
                "1",
                "userName123",
                "symbol123",
                2,
                amount,
                "1",
                "1",
                "1",
                Instant.now(),
                Instant.now(),
                "tokenType123",
                "1",
                "1",
                0);
        TestUtil.runFlow(tn, tn.partyA.startFlow(new TokenCreateByUserFlow(token)));
        List<StateAndRef<TokenState>> result = tn.partyA.transaction(()-> tn.partyA.getServices().getVaultService()
                .queryBy(TokenState.class).getStates());
        System.out.println(result.get(0).getState().getData().getName());
    }




    @Test
    public void createTokenByAdminFlow() throws ExecutionException, InterruptedException {
        Long amount = 1000L;
        Token token = new Token(
                "",
                "",
                "userName123",
                "symbol123",
                2,
                amount,
                "1",
                "123456789",
                "1008",
                Instant.now(),
                Instant.now().plusSeconds(1),
                "tokenType123",
                "123456",
                "",
                1);
        TestUtil.runFlow(tn, tn.partyA.startFlow(new TokenCreateByAdminFlow(token)));
        List<StateAndRef<TokenState>> result = tn.partyA.transaction(()-> tn.partyA.getServices().getVaultService()
                .queryBy(TokenState.class).getStates());
        System.out.println(result.get(0).getState().getData().getName());
    }

    @Test
    public void sendToken() throws FlowException, ExecutionException, InterruptedException {
        TokenSendP2P tokenPayment = new TokenSendP2P();
        List<String> inputTokenIdList = new ArrayList<>();
        inputTokenIdList.add("2d71f2eb-db60-4026-9e79-079cd1c51457");
        inputTokenIdList.add("2d71f2eb-db60-4026-9e79-079cd1c51457");
        tokenPayment.setTokenType("inputTokenIdList");
        tokenPayment.setAmount(1000L);
        tokenPayment.setToUserId("2222");
        tokenPayment.setFromUserId("1234");
        TestUtil.runFlow(tn, tn.partyA.startFlow(new TokenSendP2PFlow(tokenPayment)));
    }

    @Test
    public void createFlow() throws ExecutionException, InterruptedException {
        List<String> list = new ArrayList<>();
        list.add("123");
        list.add("123");
        list.add("123");
        String str = StringUtils.join(list, ",");
        List<String> arr = Arrays.asList(str.split(","));
        ReceiverGroup receiverGroup = new ReceiverGroup("123456789", list);
        TestUtil.runFlow(tn, tn.partyA.startFlow(new ReceiverGroupCreateFlow(receiverGroup)));
        List<StateAndRef<ReceiverGroupState>> result = tn.partyA.transaction(()-> tn.partyA.getServices().getVaultService()
                .queryBy(ReceiverGroupState.class).getStates());
        Assert.assertEquals(1, result.size());
    }


    @Test
    public void redeemFlow() throws ExecutionException, InterruptedException {

        Long amount = 1099L;
        Token token = new Token(
                "1",
                "1",
                "userName123",
                "symbol123",
                2,
                amount,
                "1",
                "1",
                "1",
                Instant.now(),
                Instant.now(),
                "tokenType123",
                "1",
                "1",
                0);
        TestUtil.runFlow(tn, tn.partyA.startFlow(new TokenCreateByUserFlow(token)));
        List<StateAndRef<TokenState>> result = tn.partyA.transaction(()-> tn.partyA.getServices().getVaultService()
                .queryBy(TokenState.class).getStates());
        System.out.println(result.get(0).getState().getData().getName());

        TestUtil.runFlow(tn, tn.partyA.startFlow(new AutoRedeemFlow(result.get(0))));
        List<StateAndRef<TokenState>> result1 = tn.partyA.transaction(()-> tn.partyA.getServices().getVaultService()
                .queryBy(TokenState.class).getStates());
        Assert.assertEquals(1, result.size());
    }



    @Test
    public void createFlow1() throws ExecutionException, InterruptedException {

//创建member：memberId
        Member member = new Member("merchant", "merchant", "shopping");
        TestUtil.runFlow(tn, tn.partyA.startFlow(new MemberCreateFlow(member)));
        List<StateAndRef<MemberState>> result = tn.partyA.transaction(()-> tn.partyA.getServices().getVaultService()
                .queryBy(MemberState.class).getStates());
        Assert.assertEquals(1, result.size());

        Member member1 = new Member("user", "user", "");
        TestUtil.runFlow(tn, tn.partyA.startFlow(new MemberCreateFlow(member1)));
        List<StateAndRef<MemberState>> result1 = tn.partyA.transaction(()-> tn.partyA.getServices().getVaultService()
                .queryBy(MemberState.class).getStates());




//        Long amount = 1200L;
//        Token token = new Token(
//                "1",
//                "1",
//                "userName111",
//                "symbol111",
//                2,
//                amount,
//                "111",
//                "user",
//                "111",
//                Instant.now(),
//                null,
//                "shopping",
//                null,
//                "",
//                0);
//        TestUtil.runFlow(tn, tn.partyA.startFlow(new TokenCreateByUserFlow(token)));
//        List<StateAndRef<TokenState>> result22 = tn.partyA.transaction(()-> tn.partyA.getServices().getVaultService()
//                .queryBy(TokenState.class).getStates());
//
//        Long amount1 = 1100L;
//        Token token1 = new Token(
//                "1",
//                "1",
//                "userName111",
//                "symbol111",
//                2,
//                amount1,
//                "111",
//                "user",
//                "111",
//                Instant.now(),
//                null,
//                "shopping",
//                null,
//                "",
//                0);
//        TestUtil.runFlow(tn, tn.partyA.startFlow(new TokenCreateByUserFlow(token1)));
//        List<StateAndRef<TokenState>> result122 = tn.partyA.transaction(()-> tn.partyA.getServices().getVaultService()
//                .queryBy(TokenState.class).getStates());


//        TokenSendP2P tokenPayment = new TokenSendP2P();
//        tokenPayment.setTokenType("shopping");
//        tokenPayment.setAmount(1000L);
//        tokenPayment.setToUserId("merchant");
//        tokenPayment.setFromUserId("user");
//        TestUtil.runFlow(tn, tn.partyA.startFlow(new TokenSendP2PFlow(tokenPayment)));
//        List<StateAndRef<TokenState>> result1223 = tn.partyA.transaction(()-> tn.partyA.getServices().getVaultService()
//                .queryBy(TokenState.class).getStates());

//        List<String> list = new ArrayList<>();
//        list.add("1234");
//        String str = StringUtils.join(list, ",");
//        List<String> arr = Arrays.asList(str.split(","));
//        ReceiverGroup receiverGroup = new ReceiverGroup("123456789", list);
//        TestUtil.runFlow(tn, tn.partyA.startFlow(new ReceiverGroupCreateFlow(receiverGroup)));
//        List<StateAndRef<ReceiverGroupState>> result0 = tn.partyA.transaction(()-> tn.partyA.getServices().getVaultService()
//                .queryBy(ReceiverGroupState.class).getStates());
//        Assert.assertEquals(1, result0.size());
//
//
//        List<String> list1 = new ArrayList<>();
//        list1.add("1234");
//        ReceiverGroup receiverGroup1 = new ReceiverGroup("1234567890", list);
//        TestUtil.runFlow(tn, tn.partyA.startFlow(new ReceiverGroupCreateFlow(receiverGroup1)));
//        List<StateAndRef<ReceiverGroupState>> result1 = tn.partyA.transaction(()-> tn.partyA.getServices().getVaultService()
//                .queryBy(ReceiverGroupState.class).getStates());


        //创建memberList：memberId
        List<String> listMerchantLis = new ArrayList<>();
        listMerchantLis.add("memberId");
        listMerchantLis.add("222");
        listMerchantLis.add("333");

        MerchantList merchantList = new MerchantList("merchantListId", listMerchantLis);
        TestUtil.runFlow(tn, tn.partyA.startFlow(new MerchantListCreateFlow(merchantList)));
        List<StateAndRef<MerchantListState>> resultMerchantLis = tn.partyA.transaction(()-> tn.partyA.getServices().getVaultService()
                .queryBy(MerchantListState.class).getStates());
        Assert.assertEquals(1, resultMerchantLis.size());

        List<String> listMerchantLis1 = new ArrayList<>();
        listMerchantLis1.add("444");
        listMerchantLis1.add("555");
        listMerchantLis1.add("666");

        MerchantList merchantList1 = new MerchantList("merchantListId1", listMerchantLis);
        TestUtil.runFlow(tn, tn.partyA.startFlow(new MerchantListCreateFlow(merchantList1)));
        List<StateAndRef<MerchantListState>> resultMerchantLis1 = tn.partyA.transaction(()-> tn.partyA.getServices().getVaultService()
                .queryBy(MerchantListState.class).getStates());

        Instant releaseDate =  Instant.now().plusMillis(TimeUnit.HOURS.toMillis(8));
        Instant expiryDate = releaseDate.plusSeconds(1);

        Token token1 = new Token(
                "",
                "",
                "userName222",
                "symbol222",
                2,
                1000L,
                "222",
                "user",
                "222",
                releaseDate,
                expiryDate,
                "shopping",
                "merchantListId,merchantListId1",
                "",
                1);

        System.out.println("###################"+expiryDate);
        TestUtil.runFlow(tn, tn.partyA.startFlow(new TokenCreateByAdminFlow(token1)));
        List<StateAndRef<TokenState>> result2 = tn.partyA.transaction(()-> tn.partyA.getServices().getVaultService()
                .queryBy(TokenState.class).getStates());

        String batchId = result2.get(0).getState().getData().getBatchId().getId().toString();

        TokenSendVoucher tokenSendVoucher = new TokenSendVoucher();
        tokenSendVoucher.setInputBatchId(batchId);
        tokenSendVoucher.setAmount(1000L);
        tokenSendVoucher.setToUserId("merchant");
        tokenSendVoucher.setFromUserId("user");
        TestUtil.runFlow(tn, tn.partyA.startFlow(new TokenSendVoucherFlow(tokenSendVoucher)));
        List<StateAndRef<TokenState>> result11 = tn.partyA.transaction(()-> tn.partyA.getServices().getVaultService()
                .queryBy(TokenState.class).getStates());

//        String id1 = result2.get(0).getState().getData().getBatchId().toString();
//
//        TokenPayment tokenPayment = new TokenPayment();
//        List<String> inputTokenIdList = new ArrayList<>();
//        inputTokenIdList.add(id1);
//        tokenPayment.setInputBatchIdList(inputTokenIdList);
//        tokenPayment.setAmount(10L);
//        tokenPayment.setToUserId("memberId");
//        tokenPayment.setFromUserId("1234");
//        TestUtil.runFlow(tn, tn.partyA.startFlow(new TokenPaymentFlow(tokenPayment)));



//        TokenPayment tokenPayment = new TokenPayment();
//        List<String> inputTokenIdList = new ArrayList<>();
//        inputTokenIdList.add(id1);
////        inputTokenIdList.add(id2);
//        tokenPayment.setInputBatchIdList(inputTokenIdList);
//        tokenPayment.setAmount(3400L);
//        tokenPayment.setToUserId("2222");
//        tokenPayment.setFromUserId("1234");
//        SignedTransaction signedTransaction = TestUtil.runFlow(tn, tn.partyA.startFlow(new TokenSplitPaymentFlow(tokenPayment)));
//
//        String txHash = signedTransaction.getId().toString();
//
//        TestUtil.runFlow(tn, tn.partyA.startFlow(new TokenSplitPaymentFinalFlow(txHash)));

    }

}
