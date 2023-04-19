package com.ehkd.blockchain;

import com.ehkd.blockchain.sdk.CordaConfig;
import com.ehkd.blockchain.sdk.CordaService;
import com.ehkd.blockchain.service.MerchantListService;
import com.ehkd.blockchain.util.CordaResult;
import com.ehkd.blockchain.util.CordaResultPage;
import com.ehkd.corda.state.MerchantListState;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import net.corda.core.contracts.StateAndRef;
import net.corda.core.node.services.Vault;
import net.corda.core.transactions.SignedTransaction;
import org.junit.Test;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Kris Lau
 * @date 2023/3/22
 */
public class MerchantListTest {

    CordaService cordaService = new CordaService(new CordaConfig());
    @Test
    public void crateMerchantList() throws Exception {
        List<String> list = new ArrayList<>();
        list.add("user1");
        list.add("merchant1");
        SignedTransaction signedTransaction = cordaService.createMerchantList("merchantListId1",list);
        System.out.println(signedTransaction.toString());
    }
//
    //查询接收者清单
    @Test
    public void searchMerchantList() throws Exception {
        Vault.Page<MerchantListState> merchantListStatePage = cordaService.searchMerchantList();
        if(merchantListStatePage != null) {
            List<StateAndRef<MerchantListState>> merchantListState = merchantListStatePage.getStates();
            if(merchantListState != null&&merchantListState.size()>0) {
                  List<String> mList = merchantListState.stream() .map(m->m.getState().getData().getMerchantListId()).collect(Collectors.toList());
                  System.out.println(mList);
            }
        }


    }

    //查询接收者清单详情
    @Test
    public void getMerchantListById() throws Exception {
        MerchantListService service = new MerchantListService(cordaService);
        CordaResultPage data = service.getMerchantListById("merchantListId", 1, 20);
        System.out.println("getReceiverListById 返回结果：+++++++++++++++++++++++++++++++++++++++");
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

    //更新接收者清单
    @Test
    public void updateMerchantList() throws Exception {
        List<String> list = new ArrayList<>();
        list.add("1234");
        list.add("2324");
        list.add("3245");
        list.add("3456");
        list.add("4567");
        MerchantListService service = new MerchantListService(cordaService);
        CordaResult<String> data = service.updateMerchantList("223456789", list);
        System.out.println("updateReceiverList 返回结果：+++++++++++++++++++++++++++++++++++++++");
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

    //增加接收者清单成员
    @Test
    public void addMerchant() throws Exception {
        List<String> list = new ArrayList<>();
        list.add("5678");
        list.add("6789");
        list.add("7890");
        MerchantListService service = new MerchantListService(cordaService);
        CordaResult<String> data = service.addMerchant("223456789", list);
        System.out.println("addMember 返回结果：+++++++++++++++++++++++++++++++++++++++");
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

    //删除接收者清单成员
    @Test
    public void deleteMerchant() throws Exception {
        List<String> list = new ArrayList<>();
        list.add("5678");
        list.add("6789");
        list.add("7890");
        MerchantListService service = new MerchantListService(cordaService);
        CordaResult<String> data = service.deleteMerchant("223456789", list);
        System.out.println("deleteMember 返回结果：+++++++++++++++++++++++++++++++++++++++");
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

}
