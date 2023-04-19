package com.ehkd.blockchain.sdk;

import com.ehkd.blockchain.entity.MemberEntity;
import com.ehkd.corda.flow.*;
import com.ehkd.corda.flow.TokenSendP2PFlow;
import com.ehkd.corda.payload.*;
import com.ehkd.corda.schema.*;
import com.ehkd.corda.state.MemberState;
import com.ehkd.corda.state.MerchantListState;
import com.ehkd.corda.state.ReceiverGroupState;
import com.ehkd.corda.state.TokenState;
import lombok.extern.log4j.Log4j2;
import net.corda.client.rpc.CordaRPCClient;
import net.corda.client.rpc.CordaRPCConnection;
import net.corda.core.concurrent.CordaFuture;
import net.corda.core.contracts.ContractState;
import net.corda.core.contracts.StateAndRef;
import net.corda.core.contracts.StateRef;
import net.corda.core.messaging.ClientRpcSslOptions;
import net.corda.core.messaging.CordaRPCOps;
import net.corda.core.node.services.Vault;
import net.corda.core.node.services.vault.*;
import net.corda.core.transactions.SignedTransaction;
import net.corda.core.utilities.NetworkHostAndPort;
import org.apache.commons.lang3.StringUtils;

import java.lang.reflect.Field;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.function.Supplier;
import java.util.stream.Collectors;

/**
 * @author Kris Lau
 * @date 2023/3/22
 */
@Log4j2
public class CordaService {

    String nodeHost = "localhost";

    String nodePort = "10006";

    String nodeLoginName = "user1";

    String nodeLoginPsw = "test";

    boolean usetls = false;

    Path certsPath = Paths.get("reccerts");

    String certsPass = "abcabc";

    private CordaRPCOps cordaRPCOps;

    private CordaRPCConnection connection;

    private static final Integer decimals = 2;

    private CordaService() {
    }

    public CordaService(String host, String port) {
        this.nodeHost = host;
        this.nodePort = port;
    }

    public CordaService(CordaConfig config) {
        this.nodeHost = config.getHost();
        this.nodePort = config.getPort();
        this.nodeLoginName = config.getUsername();
        this.nodeLoginPsw = config.getPassword();
        this.usetls = config.isUsetls();
        this.certsPath = config.getCertsPath();
        this.certsPass = config.getCertsPass();
    }

    public void getOrCreateConnection() {
        if (connection == null) {
            try {
                createCordaRPCOps();
            } catch (Exception ex) {
                log.error(ex.toString(), ex);
            }
        }
        try {
            cordaRPCOps.currentNodeTime();
        } catch (Exception e) {
            try {
                createCordaRPCOps();
            } catch (Exception ex) {
                log.error(ex.toString(), ex);
            }
        }
    }

    private void createCordaRPCOps() {
        NetworkHostAndPort nodeHP = new NetworkHostAndPort(nodeHost, Integer.parseInt(nodePort));
        log.debug("Upstream node URL: {}", nodeHP.getHost());
        CordaRPCClient client = null;
        if (usetls) {
            client = new CordaRPCClient(
                    nodeHP,
                    new ClientRpcSslOptions(
                            certsPath,
                            certsPass,
                            "JKS"
                    ),
                    null);
        } else {
            client = new CordaRPCClient(nodeHP);
        }
        log.debug("Attempting upstream RPC to node: '{}'", nodeHP);
        connection = client.start(nodeLoginName, nodeLoginPsw);
        log.debug("Successful connection. Creating proxy...");
        cordaRPCOps = connection.getProxy();
    }

    private SignedTransaction runWorkflow(Supplier<CordaFuture<SignedTransaction>> op) throws ExecutionException, InterruptedException {
        getOrCreateConnection();
        return op.get().get();
    }

    public SignedTransaction createMember(MemberEntity memberEntity) {
        getOrCreateConnection();
        Member member = new Member();
        member.setUserId(memberEntity.getUserId());
        member.setType(memberEntity.getType());
        member.setName(memberEntity.getName());
        member.setIndustry(memberEntity.getIndustry());
        try{
            SignedTransaction signedTransaction = runWorkflow(() -> cordaRPCOps.startFlowDynamic(
                            MemberCreateFlow.class,
                            member)
                    .getReturnValue()
            );
            return signedTransaction;
        } catch (Exception e) {
            log.error("Error occur during create member", e);
        }
        return null;
    }

    public <T extends ContractState> StateAndRef<T> searchMemberByUserId(String userId, Class<T> clazz){
        getOrCreateConnection();
        QueryCriteria queryCriteria = new MemberContractCriteria().getUserIdEqualCriteria(userId);
        List<StateAndRef<T>> result = cordaRPCOps.vaultQueryByCriteria(queryCriteria, clazz).getStates();
        if(result.isEmpty()) {
            throw new NoSuchElementException("No Such Element for " + clazz.getTypeName() + " with userId:" + userId);
        } else {
            return result.get(0);
        }
    }

    public StateAndRef<MemberState> searchMemberById(String id) throws NoSuchElementException{
        getOrCreateConnection();
        QueryCriteria queryCriteria = getFieldQueryCriteria(PersistentMember.class, "userId", id);
        List<StateAndRef<MemberState>> result = cordaRPCOps.vaultQueryByCriteria(queryCriteria, MemberState.class).getStates();
        if(result.isEmpty()) {
            throw new NoSuchElementException("No Such Element for userId:" + id);
        } else {
            return result.get(0);
        }
    }

    public List<StateAndRef<MemberState>> searchMemberAll(String userId) throws NoSuchElementException{
        getOrCreateConnection();
        QueryCriteria queryCriteria = null;
        if(StringUtils.isNotBlank(userId)) {
            queryCriteria = getFieldQueryCriteria(PersistentMember.class, "userId", userId);
        } else {
            queryCriteria = new QueryCriteria.VaultCustomQueryCriteria(Builder.notNull(new FieldInfo("userId",PersistentMember.class))
                    , Vault.StateStatus.UNCONSUMED);
        }
        List<StateAndRef<MemberState>> list = new ArrayList<>();
        long totalResults = 0L;
        Integer pageNumber=1, pageSize = 200;
        do {
            Vault.Page<MemberState> data = cordaRPCOps.vaultQueryByWithPagingSpec(MemberState.class, queryCriteria, getPage(pageNumber, pageSize));
            if(data != null) {
                totalResults = data.getTotalStatesAvailable();
                List<StateAndRef<MemberState>> members = data.getStates();
                if(members!=null&&members.size()>0) {
                    list.addAll(members);
                }
            }
            pageNumber++;
        } while ((pageSize * (pageNumber - 1) <= totalResults));
        return list;
    }

    public SignedTransaction UpdateMember(StateRef ref, Member updateMember) throws ExecutionException, InterruptedException{
            return runWorkflow(() -> cordaRPCOps.startFlowDynamic(
                            MemberModifyFlow.class,
                            ref,
                            updateMember
                    ).getReturnValue()
            );
    }

    private static String sum_error_msg = "net.corda.core.node.services.VaultQueryException: An error occurred while attempting to query the vault: List is empty.";
    public Vault.Page<ContractState> searchTokenSummary(String owner, List<String> merchants, String tokenType, Integer isVoucher
            , List<String> groupBy, Integer pageNumber, Integer pageSize) {
        getOrCreateConnection();
        List<Field> groupByFields = null;
        if(groupBy!= null&&groupBy.size()>0) {
            groupByFields = groupBy.stream().map(m-> CordaService.getField(m, PersistentToken.class)).collect(Collectors.toList());
        }
        Field amountField = CordaService.getField("amount", PersistentToken.class);
        QueryCriteria sumCriteria = new QueryCriteria.VaultCustomQueryCriteria(Builder.sum(amountField, groupByFields, Sort.Direction.DESC));
        sumCriteria = sumCriteria.and(CordaService.getFieldQueryCriteria(PersistentToken.class, "isVoucher", isVoucher==null?"1":isVoucher.toString()));
        if(StringUtils.isNotBlank(tokenType)) {
            sumCriteria = sumCriteria.and(CordaService.getFieldQueryCriteria(PersistentToken.class, "tokenType", tokenType));
        }
        if(StringUtils.isNotBlank(owner)) {
            sumCriteria = sumCriteria.and(CordaService.getFieldQueryCriteria(PersistentToken.class, "owner", owner));
        }
        if(merchants!=null&&merchants.size()>0) {
            FieldInfo merchantList = new FieldInfo("merchantList", PersistentToken.class);
            QueryCriteria merchanQuery = null;
            for(String merchantId:merchants) {
                if(merchanQuery == null) {
                    merchanQuery = new QueryCriteria.VaultCustomQueryCriteria(Builder.equal(merchantList, merchantId), Vault.StateStatus.UNCONSUMED);
                } else {
                    merchanQuery = merchanQuery.or(new QueryCriteria.VaultCustomQueryCriteria(Builder.equal(merchantList, merchantId), Vault.StateStatus.UNCONSUMED));
                }
                merchanQuery = merchanQuery.or(new QueryCriteria.VaultCustomQueryCriteria(Builder.like(merchantList, merchantId+",%"), Vault.StateStatus.UNCONSUMED))
                        .or(new QueryCriteria.VaultCustomQueryCriteria(Builder.like(merchantList, "%,"+merchantId), Vault.StateStatus.UNCONSUMED))
                        .or(new QueryCriteria.VaultCustomQueryCriteria(Builder.like(merchantList, "%,"+merchantId+",%"), Vault.StateStatus.UNCONSUMED));
            }
            sumCriteria = sumCriteria.and(merchanQuery);
        }
        try{
            return cordaRPCOps.vaultQueryByWithPagingSpec(ContractState.class,sumCriteria, getPage(pageNumber, pageSize));
        } catch (Exception e) {
            if(sum_error_msg.equals(e.getMessage())) {
                log.info("数据库没有值做sum()函数抛的异常，不做处理");
                return null;
            } else {
                throw e;
            }
        }
    }

    public List<TokenState> searchTokenList(String owner, String tokenType, Integer isVoucher) {
        Field amountField = CordaService.getField("amount", PersistentToken.class);
        QueryCriteria queryCriteria = new QueryCriteria.VaultCustomQueryCriteria(Builder.notNull(amountField), Vault.StateStatus.UNCONSUMED);
        if(isVoucher != null) {
            queryCriteria = queryCriteria.and(CordaService.getFieldQueryCriteria(PersistentToken.class, "isVoucher", isVoucher.toString()));
        }
        if(StringUtils.isNotBlank(tokenType)) {
            queryCriteria = queryCriteria.and(CordaService.getFieldQueryCriteria(PersistentToken.class, "tokenType", tokenType));
        }
        if(StringUtils.isNotBlank(owner)) {
            queryCriteria = queryCriteria.and(CordaService.getFieldQueryCriteria(PersistentToken.class, "owner", owner));
        }
        List<TokenState> list = new ArrayList<>();
        long totalResults = 0L;
        Integer pageNumber=1, pageSize = 200;
        do {
            getOrCreateConnection();
            Vault.Page<TokenState> data = cordaRPCOps.vaultQueryByWithPagingSpec(TokenState.class, queryCriteria, getPage(pageNumber, pageSize));
            if(data != null) {
                totalResults = data.getTotalStatesAvailable();
                List<StateAndRef<TokenState>> tokens = data.getStates();
                if(tokens!=null&&tokens.size()>0) {
                    tokens.forEach(t->list.add(t.getState().getData()));
                }
            }
            pageNumber++;
        } while ((pageSize * (pageNumber - 1) <= totalResults));
        return list;
    }

    public List<TokenState> searchTokenList(String owner, List<String> merchants, String tokenType, Integer isVoucher) {
        Field amountField = CordaService.getField("amount", PersistentToken.class);
        QueryCriteria queryCriteria = new QueryCriteria.VaultCustomQueryCriteria(Builder.notNull(amountField), Vault.StateStatus.UNCONSUMED);
        if(isVoucher != null) {
            queryCriteria = queryCriteria.and(CordaService.getFieldQueryCriteria(PersistentToken.class, "isVoucher", isVoucher.toString()));
        }
        if(StringUtils.isNotBlank(tokenType)) {
            queryCriteria = queryCriteria.and(CordaService.getFieldQueryCriteria(PersistentToken.class, "tokenType", tokenType));
        }
        if(StringUtils.isNotBlank(owner)) {
            queryCriteria = queryCriteria.and(CordaService.getFieldQueryCriteria(PersistentToken.class, "owner", owner));
        }
        if(merchants!=null&&merchants.size()>0) {
            FieldInfo merchantList = new FieldInfo("merchantList", PersistentToken.class);
            QueryCriteria merchanQuery = new QueryCriteria.VaultCustomQueryCriteria(Builder.in(merchantList, merchants), Vault.StateStatus.UNCONSUMED);
            queryCriteria = merchanQuery.and(queryCriteria);
        }
        List<TokenState> list = new ArrayList<>();
        long totalResults = 0L;
        Integer pageNumber=1, pageSize = 200;
        do {
            getOrCreateConnection();
            Vault.Page<TokenState> data = cordaRPCOps.vaultQueryByWithPagingSpec(TokenState.class, queryCriteria, getPage(pageNumber, pageSize));
            if(data != null) {
                totalResults = data.getTotalStatesAvailable();
                List<StateAndRef<TokenState>> tokens = data.getStates();
                if(tokens!=null&&tokens.size()>0) {
                    tokens.forEach(t->list.add(t.getState().getData()));
                }
            }
            pageNumber++;
        } while ((pageSize * (pageNumber - 1) <= totalResults));
        return list;
    }

    public TokenState searchTokenOne(String userId, String tokenId) {
        getOrCreateConnection();
        QueryCriteria queryCriteria = CordaService.getFieldQueryCriteria(PersistentToken.class, "batchId", UUID.fromString(tokenId))
                .and(CordaService.getFieldQueryCriteria(PersistentToken.class, "owner", userId));
        Vault.Page<TokenState> result = cordaRPCOps.vaultQueryByWithPagingSpec(TokenState.class, queryCriteria, new PageSpecification(1,1));
        if(result!=null) {
            List<StateAndRef<TokenState>> list = result.getStates();
            if(list!=null&&list.size()>0) {
                TokenState tokenState = list.get(0).getState().getData();
                tokenState.setAmount(result.getStates().stream().mapToLong(m->m.getState().getData().getAmount()).sum());
                return tokenState;
            }
        }
        return null;
    }

    public TokenState searchBatchTokenOne(String batchId) {
        getOrCreateConnection();
        QueryCriteria queryCriteria = CordaService.getFieldQueryCriteria(PersistentToken.class, "batchId", UUID.fromString(batchId));
        Vault.Page<TokenState> result = cordaRPCOps.vaultQueryByWithPagingSpec(TokenState.class, queryCriteria, new PageSpecification(1, 1));
        if(result!=null) {
            List<StateAndRef<TokenState>> list = result.getStates();
            if(list!=null&&list.size()>0) {
                return list.get(0).getState().getData();
            }
        }
        return null;
    }

    public static QueryCriteria getFieldQueryCriteria(Class cls,String fieldName, Object fieldValue) {
        return new QueryCriteria.VaultCustomQueryCriteria(Builder.equal(getField(fieldName, cls), fieldValue), Vault.StateStatus.UNCONSUMED);
    }

    public static Field getField(String fieldName, Class cls) {
        try {
            return cls.getField(fieldName);
        } catch (NoSuchFieldException e) {
            return null;
        }
    }


    public SignedTransaction mintTokenByUser(String userId, String name, String symbol, Long amount, String tokenType) throws Exception {
        getOrCreateConnection();
        Token token = new Token();
        token.setName(name);
        token.setSymbol(symbol);
        token.setDecimals(decimals);
        token.setAmount(amount);
        token.setTokenType(tokenType);
        token.setOwner(userId);
        token.setSponsor(userId);
        token.setIssuer(userId);
        token.setReleaseDate(Instant.now());
        token.setMerchantList("");
        token.setLockedBy("");
        token.setIsVoucher(0);
        try{
            SignedTransaction signedTransaction = runWorkflow(() -> cordaRPCOps.startFlowDynamic(
                            TokenCreateByUserFlow.class,
                            token)
                    .getReturnValue()
            );
            return signedTransaction;
        } catch (Exception e) {
            log.error("Error occur during mint token by user", e);
            throw new Exception(e.getMessage());
        }
    }

    public SignedTransaction mintTokenByAdmin(String name, String symbol, Long amount, String tokenType, String sponsorId, String issuerId, Date releaseDate, Date expiryDate, String merchantListId, String receiverIdStr) throws Exception{

        Instant releaseInstant = Instant.ofEpochMilli(releaseDate.getTime());
        Instant expiryInstant = Instant.ofEpochMilli(expiryDate.getTime());
        getOrCreateConnection();
        Token token = new Token();
        token.setTokenId("");
        token.setBatchId("");
        token.setName(name);
        token.setSymbol(symbol);
        token.setDecimals(decimals);
        token.setAmount(amount);
        token.setTokenType(tokenType);
        token.setOwner(receiverIdStr);
        token.setSponsor(sponsorId);
        token.setIssuer(issuerId);
        token.setReleaseDate(releaseInstant);
        token.setExpiryDate(expiryInstant);
        token.setMerchantList(merchantListId);
        token.setLockedBy("");
        token.setIsVoucher(1);
        try{
            SignedTransaction signedTransaction = runWorkflow(() -> cordaRPCOps.startFlowDynamic(
                            TokenCreateByAdminFlow.class,
                            token)
                    .getReturnValue()
            );
            return signedTransaction;
        } catch (Exception e) {
            log.error("Error occur during mint token by admin", e);
            throw new Exception(e.getMessage());
        }
    }

    public SignedTransaction mintLockedToken(String owner, String bankId,String name, String symbol, Long amount, String tokenType) throws Exception {
        getOrCreateConnection();
        Token token = new Token();
        token.setName(name);
        token.setSymbol(symbol);
        token.setDecimals(decimals);
        token.setAmount(amount);
        token.setTokenType(tokenType);
        token.setOwner(owner);
        token.setSponsor(bankId);
        token.setIssuer(bankId);
        token.setReleaseDate(Instant.now());
        token.setMerchantList("");
        token.setLockedBy(bankId);
        token.setIsVoucher(0);
        try{
            SignedTransaction signedTransaction = runWorkflow(() -> cordaRPCOps.startFlowDynamic(
                            TokenCreateByUserFlow.class,
                            token)
                    .getReturnValue()
            );
            return signedTransaction;
        } catch (Exception e) {
            log.error("Error occur during mint lock token", e);
            throw new Exception(e.getMessage());
        }
    }

    public SignedTransaction unLockToken(String tokenId) throws Exception{
        getOrCreateConnection();

        try{
            SignedTransaction signedTransaction = runWorkflow(() -> cordaRPCOps.startFlowDynamic(
                            TokenUnLockFlow.class,
                            tokenId)
                    .getReturnValue()
            );
            return signedTransaction;
        } catch (Exception e) {
            log.error("Error occur during token payment", e);
            throw new Exception(e.getMessage());
        }
    }


    public SignedTransaction createReceiverGroup(String groupId,List<String> members) throws Exception{
        getOrCreateConnection();
        ReceiverGroup receiverGroup = new ReceiverGroup();
        receiverGroup.setGroupId(groupId);
        receiverGroup.setMembers(members);
        try{
            SignedTransaction signedTransaction = runWorkflow(() -> cordaRPCOps.startFlowDynamic(
                            ReceiverGroupCreateFlow.class,
                            receiverGroup)
                    .getReturnValue()
            );
            return signedTransaction;
        } catch (Exception e) {
            log.error("Error occur during create receiver group", e);
            throw new Exception(e.getMessage());
        }
    }

    public SignedTransaction sendVoucher(String batchId, Long amount, String fromUserId, String toUserId) throws Exception{
        getOrCreateConnection();
        TokenSendVoucher tokenSendVoucher = new TokenSendVoucher();
        tokenSendVoucher.setInputBatchId(batchId);
        tokenSendVoucher.setAmount(amount);
        tokenSendVoucher.setFromUserId(fromUserId);
        tokenSendVoucher.setToUserId(toUserId);

        try{
            SignedTransaction signedTransaction = runWorkflow(() -> cordaRPCOps.startFlowDynamic(
                            TokenSendVoucherFlow.class,
                            tokenSendVoucher)
                    .getReturnValue()
            );
            return signedTransaction;
        } catch (Exception e) {
            log.error("Error occur during token send voucher", e);
            throw new Exception(e.getMessage());
        }
    }


    public SignedTransaction sendP2PToken(String tokenType, Long amount, String fromUserId, String toUserId) throws Exception{
        getOrCreateConnection();
        TokenSendP2P tokenSendP2P = new TokenSendP2P();
        tokenSendP2P.setTokenType(tokenType);
        tokenSendP2P.setAmount(amount);
        tokenSendP2P.setFromUserId(fromUserId);
        tokenSendP2P.setToUserId(toUserId);

        try{
            SignedTransaction signedTransaction = runWorkflow(() -> cordaRPCOps.startFlowDynamic(
                            TokenSendP2PFlow.class,
                            tokenSendP2P)
                    .getReturnValue()
            );
            return signedTransaction;
        } catch (Exception e) {
            log.error("Error occur during token payment", e);
            throw new Exception(e.getMessage());
        }
    }


    public SignedTransaction tokenSplitPayment(List<String> batchIdList, Long amount, String fromUserId, String toUserId) throws Exception{
        getOrCreateConnection();
        TokenSendVoucher tokenPayment = new TokenSendVoucher();
        tokenPayment.setInputBatchId(batchIdList.get(0));
        tokenPayment.setAmount(amount);
        tokenPayment.setFromUserId(fromUserId);
        tokenPayment.setToUserId(toUserId);

        try{
            SignedTransaction signedTransaction = runWorkflow(() -> cordaRPCOps.startFlowDynamic(
                            TokenSplitPaymentFlow.class,
                            tokenPayment)
                    .getReturnValue()
            );
            return signedTransaction;
        } catch (Exception e) {
            log.error("Error occur during split payment", e);
            throw new Exception(e.getMessage());
        }
    }


    public SignedTransaction tokenSplitPaymentFinal(String txHash) throws Exception{
        getOrCreateConnection();

        try{
            SignedTransaction signedTransaction = runWorkflow(() -> cordaRPCOps.startFlowDynamic(
                            TokenSplitPaymentFinalFlow.class,
                            txHash)
                    .getReturnValue()
            );
            return signedTransaction;
        } catch (Exception e) {
            log.error("Error occur during split payment final", e);
            throw new Exception(e.getMessage());
        }
    }

    public Vault.Page<ReceiverGroupState> searchReceiverGroup() throws Exception{
        getOrCreateConnection();
        try {
            QueryCriteria queryCriteria = new QueryCriteria.VaultCustomQueryCriteria(Builder.notNull(new FieldInfo("groupId"
                    , PersistentReceiverGroup.class)), Vault.StateStatus.UNCONSUMED);
            return cordaRPCOps.vaultQueryByCriteria(queryCriteria, ReceiverGroupState.class);
        }catch (Exception e){
            throw new Exception(e.getMessage());
        }
    }

    public StateAndRef<ReceiverGroupState> getReceiverListById(String groupId) throws Exception{
        getOrCreateConnection();
        try {
            QueryCriteria queryCriteria = CordaService.getFieldQueryCriteria(PersistentReceiverGroup.class, "groupId", groupId);
            Vault.Page<ReceiverGroupState> states = cordaRPCOps.vaultQueryByWithPagingSpec(ReceiverGroupState.class, queryCriteria, getPage(1, 1));
            if (states != null) {
                List<StateAndRef<ReceiverGroupState>> list = states.getStates();
                if (list != null && list.size() > 0) {
                    return list.get(0);
                }
            }
        }catch (Exception e){
            throw new Exception(e.getMessage());
        }
        return null;
    }

    public SignedTransaction updateReceiverGroup(StateRef ref,List<String> members) throws Exception{
        getOrCreateConnection();
        try{
            SignedTransaction signedTransaction = runWorkflow(() -> cordaRPCOps.startFlowDynamic(
                            ReceiverGroupUpdateFlow.class,
                            ref,
                            members)
                    .getReturnValue()
            );
            return signedTransaction;
        } catch (Exception e) {
            log.error("Error occur during update member", e);
            throw new Exception(e.getMessage());
        }
    }

    public PageSpecification getPage(Integer pageNumber, Integer pageSize) {
        if(pageNumber == null || pageNumber<1) {
            pageNumber = 1;
        }
        if(pageSize == null || pageSize<1) {
            pageSize = 20;
        }
        return new PageSpecification(pageNumber, pageSize);
    }

    public SignedTransaction createMerchantList(String merchantListId,List<String> merchantList) throws Exception{
        getOrCreateConnection();
        MerchantList merchantList1 = new MerchantList();
        merchantList1.setMerchantListId(merchantListId);
        merchantList1.setMerchantList(merchantList);
        try{
            SignedTransaction signedTransaction = runWorkflow(() -> cordaRPCOps.startFlowDynamic(
                            MerchantListCreateFlow.class,
                            merchantList1)
                    .getReturnValue()
            );
            return signedTransaction;
        } catch (Exception e) {
            log.error("Error occur during create member", e);
            throw new Exception(e.getMessage());
        }
    }

    public Vault.Page<MerchantListState> searchMerchantList() throws Exception{
        getOrCreateConnection();
        try {
            QueryCriteria queryCriteria = new QueryCriteria.VaultCustomQueryCriteria(Builder.notNull(new FieldInfo("merchantListId"
                    , PersistentMerchantList.class)), Vault.StateStatus.UNCONSUMED);
            return cordaRPCOps.vaultQueryByCriteria(queryCriteria, MerchantListState.class);
        }catch (Exception e){
            throw new Exception(e.getMessage());
        }
    }

    public StateAndRef<MerchantListState> getMerchantListById(String merchantListId){
        getOrCreateConnection();
        QueryCriteria queryCriteria = CordaService.getFieldQueryCriteria(PersistentMerchantList.class, "merchantListId", merchantListId);
        Vault.Page<MerchantListState> states = cordaRPCOps.vaultQueryByWithPagingSpec(MerchantListState.class, queryCriteria, getPage(1, 1));
        if(states != null) {
            List<StateAndRef<MerchantListState>> list = states.getStates();
            if(list != null && list.size()>0) {
                return list.get(0);
            }
        }
        return null;
    }

    public Vault.Page<MerchantListState> getMerchantListIds(String merchantId, Integer pageNumber, Integer pageSize){
        getOrCreateConnection();
        Field field = getField("merchantList", PersistentMerchantList.class);
        QueryCriteria queryCriteria = new QueryCriteria.VaultCustomQueryCriteria(Builder.equal(field, merchantId), Vault.StateStatus.UNCONSUMED);
        queryCriteria = queryCriteria.or(new QueryCriteria.VaultCustomQueryCriteria(Builder.like(field, merchantId+",%"), Vault.StateStatus.UNCONSUMED))
                .or(new QueryCriteria.VaultCustomQueryCriteria(Builder.like(field, "%,"+merchantId), Vault.StateStatus.UNCONSUMED))
                .or(new QueryCriteria.VaultCustomQueryCriteria(Builder.like(field, "%,"+merchantId+",%"), Vault.StateStatus.UNCONSUMED));
        return cordaRPCOps.vaultQueryByWithPagingSpec(MerchantListState.class, queryCriteria, getPage(pageNumber, pageSize));
    }

    public SignedTransaction updateMerchantList(StateRef ref,List<String> merchantList) throws Exception{
        getOrCreateConnection();
        try{
            SignedTransaction signedTransaction = runWorkflow(() -> cordaRPCOps.startFlowDynamic(
                            MerchantListUpdateFlow.class,
                            ref,
                            merchantList)
                    .getReturnValue()
            );
            return signedTransaction;
        } catch (Exception e) {
            log.error("Error occur during update member", e);
            throw new Exception(e.getMessage());
        }
    }


    public Vault.Page<TokenState> searchTokenUnconsumed(String batchId) throws Exception{
        getOrCreateConnection();
        try {

            QueryCriteria queryCriteria = new QueryCriteria.VaultCustomQueryCriteria(Builder.equal(PersistentToken.class.getField("batchId"), UUID.fromString(batchId)),
                    Vault.StateStatus.UNCONSUMED);
            return cordaRPCOps.vaultQueryByCriteria(queryCriteria, TokenState.class);
        }catch (Exception e){
            throw new Exception(e.getMessage());
        }
    }

    public Vault.Page<TokenState> searchTokenAll(String batchId) throws Exception{
        getOrCreateConnection();
        try {

            QueryCriteria queryCriteria = new QueryCriteria.VaultCustomQueryCriteria(Builder.equal(PersistentToken.class.getField("batchId"), UUID.fromString(batchId)),
                    Vault.StateStatus.ALL);
            return cordaRPCOps.vaultQueryByCriteria(queryCriteria, TokenState.class);
        }catch (Exception e){
            throw new Exception(e.getMessage());
        }
    }
}