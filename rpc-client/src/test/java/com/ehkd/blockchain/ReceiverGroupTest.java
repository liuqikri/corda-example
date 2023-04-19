package com.ehkd.blockchain;

import com.ehkd.blockchain.sdk.CordaConfig;
import com.ehkd.blockchain.sdk.CordaService;
import com.ehkd.blockchain.service.ReceiverGroupService;
import com.ehkd.blockchain.util.CordaResult;
import com.ehkd.blockchain.util.CordaResultPage;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import net.corda.core.transactions.SignedTransaction;
import org.junit.Test;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Kris Lau
 * @date 2023/3/22
 */
public class ReceiverGroupTest {

    CordaService cordaService = new CordaService(new CordaConfig());

    @Test
    public void crateReceiverGroup() throws Exception {
        List<String> list = new ArrayList<>();
        list.add("fromUser1");
        list.add("fromUser11");
        SignedTransaction signedTransaction = cordaService.createReceiverGroup("groupId1",list);
        System.out.println(signedTransaction.toString());
    }

    //查询接收者清单
    @Test
    public void searchReceiverGroup() throws Exception {
        
        ReceiverGroupService service = new ReceiverGroupService(cordaService);
        CordaResult<List<String>> data = service.searchReceiverGroup();
        System.out.println("searchReceiverGroup 返回结果：+++++++++++++++++++++++++++++++++++++++");
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

    //查询接收者清单详情
    @Test
    public void getReceiverListById() throws Exception {
        
        ReceiverGroupService service = new ReceiverGroupService(cordaService);
        CordaResultPage data = service.getReceiverListById("123456789", 1, 20);
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
    public void updateReceiverList() throws Exception {
        List<String> list = new ArrayList<>();
        list.add("1234");
        list.add("2324");
        list.add("3245");
        list.add("3456");
        list.add("4567");
        
        ReceiverGroupService service = new ReceiverGroupService(cordaService);
        CordaResult<String> data = service.updateReceiverList("123456789", list);
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
    public void addMember() throws Exception {
        List<String> list = new ArrayList<>();
        list.add("5678");
        list.add("6789");
        list.add("7890");
        
        ReceiverGroupService service = new ReceiverGroupService(cordaService);
        CordaResult<String> data = service.addMember("123456789", list);
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
    public void deleteMember() throws Exception {
        List<String> list = new ArrayList<>();
        list.add("5678");
        list.add("6789");
        list.add("7890");
        
        ReceiverGroupService service = new ReceiverGroupService(cordaService);
        CordaResult<String> data = service.deleteMember("123456789", list);
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
