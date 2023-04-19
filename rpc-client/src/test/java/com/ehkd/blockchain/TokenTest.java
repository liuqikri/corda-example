package com.ehkd.blockchain;

import com.ehkd.blockchain.model.MintTokenModel;
import com.ehkd.blockchain.model.TokenBalanceSummaryModel;
import com.ehkd.blockchain.model.TokenDetailModel;
import com.ehkd.blockchain.model.VoucherBalanceSummaryModel;
import com.ehkd.blockchain.sdk.CordaService;
import com.ehkd.blockchain.service.MintService;
import com.ehkd.blockchain.service.SendService;
import com.ehkd.blockchain.service.TokenSearchService;
import com.ehkd.blockchain.util.CordaResult;
import com.ehkd.blockchain.util.CordaResultPage;
import com.ehkd.blockchain.util.DateUtils;
import com.ehkd.corda.state.TokenState;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.log4j.Log4j2;
import net.corda.core.node.services.Vault;
import net.corda.core.transactions.SignedTransaction;
import org.junit.Test;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.*;

/**
 * @author Kris Lau
 * @date 2023/3/22
 */
@Log4j2
public class TokenTest {

//    CordaService cordaService = new CordaService(new CordaConfig());

    CordaService cordaService = new CordaService("172.21.93.230","10006");
    @Test
    public void crateTokenByUser() {
        MintService mintService = new MintService(cordaService);
        CordaResult<MintTokenModel> result = mintService.mintByUser("fromUser", "tokenName1", "symbol1", 1200L, "merchant");
        System.out.println(result);

    }

    /**
     * 测试统计所有个人token及余额
     * @throws Exception
     */
    @Test
    public void crateTokenByAdmin() throws Exception {

        List<String> merchantListIds = new ArrayList<>();
        List<String> receiverGroupIds = new ArrayList<>();
        merchantListIds.add("merchant");
//        merchantListIds.add("merchantListId1");
        receiverGroupIds.add("user");
//        receiverGroupIds.add("1groupId1");
        MintService mintService = new MintService(cordaService);
        Date date = new Date();
        Calendar calendar = new GregorianCalendar();
        calendar.setTime(date);
        // 把日期往后增加一天,整数  往后推,负数往前移动
        calendar.add(Calendar.DATE, 10);
        // 这个时间就是日期往后推一天的结果
        date = calendar.getTime();

        CordaResult<MintTokenModel> result = mintService.mintByAdmin("tokenName1", "symbol1", 2000L,
                "shopping", "sponsorId","issuerId", new Date(),date,merchantListIds
                ,receiverGroupIds);
//        SignedTransaction signedTransaction = cordaService.mintTokenByAdmin("tokenName1", "symbol1", 2000L,
//                "shopping", "sponsorId","issuerId", new Date(),date,
//                "merchantListId1","groupId");
        System.out.println(result);

    }


    @Test
    public void mintLockedToken() throws Exception {
        MintService mintService = new MintService(cordaService);
        CordaResult<MintTokenModel> result = mintService.mintLockedToken(3333L,"merchantId","bankId");
        System.out.println(result);
    }
    @Test
    public void unLockToken() throws Exception {
        MintService mintService = new MintService(cordaService);
        CordaResult<String> result = mintService.unLockToken("3e5769b6-125b-40c5-9f09-03b75288050e");
        System.out.println(result);
    }


    @Test
    public void searchTokenUnconsumed1() throws Exception {
        Vault.Page<TokenState> a = cordaService.searchTokenUnconsumed("4239627c-f11b-4d1c-86a9-37c1282d7a7c");
        System.out.println(a);

        Vault.Page<TokenState> b = cordaService.searchTokenAll("4239627c-f11b-4d1c-86a9-37c1282d7a7c");
        System.out.println(b);

    }

    @Test
    public void tokenPayment() throws Exception{

        SendService service = new SendService(cordaService);

        String tokenType = "shopping";
        
        List<String> inputTokenIdList = new ArrayList<>();

        BigDecimal amountBigDecimal = new BigDecimal("80");
        CordaResult<String> result = service.sendP2PToken("merchant",amountBigDecimal.longValue(),"fromUser","merchant");
        System.out.println(result.getMessage());
    }

    @Test
    public void sendVoucher() throws Exception{

        SendService service = new SendService(cordaService);

        String tokenType = "shopping";

        List<String> inputTokenIdList = new ArrayList<>();

        BigDecimal amountBigDecimal = new BigDecimal("80");
        CordaResult<String> result = service.sendVoucher("1093c8e3-d817-490c-b7bf-9e5a465d36f1",amountBigDecimal.longValue(),"user","merchant");
        System.out.println(result.getMessage());
    }



    @Test
    public void tokenSlitPayment() throws Exception{
        
        List<String> inputTokenIdList = new ArrayList<>();
        inputTokenIdList.add("8a9949fa-71b3-4b16-9948-58834a810476");
        inputTokenIdList.add("544a0245-2dc8-4d8c-aceb-426c3f5ac613");
        SignedTransaction signedTransaction = cordaService.tokenSplitPayment(inputTokenIdList,3000L,"3333","6666");
        System.out.println(signedTransaction.toString());
    }



    @Test
    public void splitPaymentFinal() throws Exception {
        SignedTransaction signedTransaction = cordaService.tokenSplitPaymentFinal("08BA22822FDE38E3AAD7B424637ACCD26B7CBE832112F42CC3EE44FA4A7EFB8E");
        System.out.println(signedTransaction.toString());
    }

    @Test
        public void searchAllTokenSummary1() throws Exception {
        log.info("开始");
        Long startTime = System.currentTimeMillis();
        
        TokenSearchService tokenSearchService = new TokenSearchService(cordaService);
        CordaResult<List<TokenBalanceSummaryModel>> data = tokenSearchService.searchAllTokenSummary();
        Long endTime = System.currentTimeMillis();
        System.out.println(endTime - startTime);
        System.out.println("searchAllTokenSummary 返回结果：+++++++++++++++++++++++++++++++++++++++");
        if(data==null) {
            System.out.println("[]");
        } else {
            ObjectMapper objectMapper = new ObjectMapper();
            objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
            System.out.println(objectMapper.writeValueAsString(data));
        }

    }

    @Test
    public void searchAllVoucherSummary() throws Exception {
        log.info("开始");
        Long staratTime = System.currentTimeMillis();

        TokenSearchService tokenSearchService = new TokenSearchService(cordaService);
        CordaResult<List<VoucherBalanceSummaryModel>> data = tokenSearchService.searchAllVoucherSummary();
        Long endTime = System.currentTimeMillis();
        System.out.println(endTime - staratTime);
        System.out.println("searchAllVoucherSummary 返回结果：+++++++++++++++++++++++++++++++++++++++");
        if(data==null) {
            System.out.println("[]");
        } else {
            ObjectMapper objectMapper = new ObjectMapper();
            objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
            System.out.println(objectMapper.writeValueAsString(data));
        }

    }

    /**
     * 测试 根据 tokenId 查询单个token详情
     * @throws Exception
     */
    @Test
    public void searchTokenOne() throws Exception {
        
        TokenSearchService tokenSearchService = new TokenSearchService(cordaService);
        CordaResult<TokenDetailModel> data = tokenSearchService.searchTokenOne("fromUser", "766c56da-e865-4e91-8195-0134c3594411");
        System.out.println("searchTokenOne 返回结果：+++++++++++++++++++++++++++++++++++++++");
        if(data==null) {
            System.out.println("{}");
        } else {
            ObjectMapper objectMapper = new ObjectMapper();
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            objectMapper.setDateFormat(dateFormat);
            objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
            System.out.println(objectMapper.writeValueAsString(data));
        }
    }


    /**
     * 测试 统计归属 userId 用户的个人token及余额
     * @throws Exception
     */
    @Test
    public void searchTokenSummaryByUser() throws Exception {
        
        TokenSearchService tokenSearchService = new TokenSearchService(cordaService);
        CordaResult<List<TokenBalanceSummaryModel>> data = tokenSearchService.searchTokenSummaryByUser("fromUser");
        System.out.println("searchTokenSummaryByUser 返回结果：+++++++++++++++++++++++++++++++++++++++");
        if(data==null) {
            System.out.println("[]");
        } else {
            ObjectMapper objectMapper = new ObjectMapper();
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            objectMapper.setDateFormat(dateFormat);
            objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
            System.out.println(objectMapper.writeValueAsString(data));
        }
    }

    /**
     * 测试 统计归属 userId 用户的可在商户中使用的个人token及余额
     * @throws Exception
     */
    @Test
    public void searchTokenByReceiver() throws Exception {

        TokenSearchService tokenSearchService = new TokenSearchService(cordaService);
        CordaResult<TokenBalanceSummaryModel> data = tokenSearchService.searchTokenByReceiver("user1","merchant1");
        System.out.println("searchTokenByReceiver 返回结果：+++++++++++++++++++++++++++++++++++++++");
        if(data==null) {
            System.out.println("[]");
        } else {
            ObjectMapper objectMapper = new ObjectMapper();
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            objectMapper.setDateFormat(dateFormat);
            objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
            System.out.println(objectMapper.writeValueAsString(data));
        }
    }

    /**
     * 测试 统计归属 userId 用户的机构发行token及余额
     * @throws Exception
     */
    @Test
    public void searchVouchersSummaryByUser() throws Exception {
        TokenSearchService tokenSearchService = new TokenSearchService(cordaService);
        CordaResultPage<List<TokenDetailModel>> data = tokenSearchService.searchVoucherSummaryByUser("fromUser"
                , 2, 1);
        System.out.println("searchVoucherSummaryByUser 返回结果：+++++++++++++++++++++++++++++++++++++++");
        if(data==null) {
            System.out.println("[]");
        } else {
            ObjectMapper objectMapper = new ObjectMapper();
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            objectMapper.setDateFormat(dateFormat);
            objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
            System.out.println(objectMapper.writeValueAsString(data));
        }
    }

    /**
     * 测试 获取用户 userId 名下的所有机构发行token集合
     * @throws Exception
     */
    @Test
    public void searchVoucherByReceiver() throws Exception {
        TokenSearchService tokenSearchService = new TokenSearchService(cordaService);
        CordaResult<List<TokenDetailModel>> data = tokenSearchService.searchVoucherByReceiver("user1","user1");
        System.out.println("searchVoucherByReceiver 返回结果：+++++++++++++++++++++++++++++++++++++++");
        if(data==null) {
            System.out.println("[]");
        } else {
            ObjectMapper objectMapper = new ObjectMapper();
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            objectMapper.setDateFormat(dateFormat);
            objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
            System.out.println(objectMapper.writeValueAsString(data));
        }
    }

    @Test
    public void testInstant(){

        Instant instant = Instant.now();
        System.out.println(instant);
        Date date1 =  DateUtils.instantToDate(instant);
        System.out.println(date1);
        Instant instant1 = Instant.ofEpochMilli(date1.getTime());
        System.out.println(instant1);

    }





}
