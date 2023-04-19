package com.ehkd.blockchain.service;

import com.ehkd.blockchain.model.TokenBalanceSummaryModel;
import com.ehkd.blockchain.model.TokenDetailModel;
import com.ehkd.blockchain.model.VoucherBalanceSummaryModel;
import com.ehkd.blockchain.sdk.CordaService;
import com.ehkd.blockchain.util.CordaResult;
import com.ehkd.blockchain.util.CordaResultCodeEnum;
import com.ehkd.blockchain.util.CordaResultPage;
import com.ehkd.blockchain.util.PageUtils;
import com.ehkd.corda.state.MemberState;
import com.ehkd.corda.state.MerchantListState;
import com.ehkd.corda.state.TokenState;
import com.google.common.collect.Lists;
import lombok.extern.log4j.Log4j2;
import net.corda.core.contracts.StateAndRef;
import net.corda.core.node.services.Vault;
import org.apache.commons.lang3.StringUtils;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.stream.Collectors;

@Log4j2
public class TokenSearchService {

    private TokenSearchService() {
    }
    public TokenSearchService(CordaService cordaService) {
        this.cordaService = cordaService;
    }

    CordaService cordaService;

    /**
     * 获取所有个人发行token，根据tokenType汇总余额
     * @return
     */
    public CordaResult<List<TokenBalanceSummaryModel>> searchAllTokenSummary() {
        CordaResult result = CordaResult.success(null);
        try{
            List<TokenState> data = cordaService.searchTokenList(null, null
                    , 0);
            if(data != null&&data.size()>0) {
                List<TokenBalanceSummaryModel> list = new ArrayList<>();
                data.stream().sorted(Comparator.comparing(TokenState::getTokenType))
                        .collect(Collectors.groupingBy(
                                TokenState::getTokenType
                                , LinkedHashMap::new
                                , Collectors.summingLong(TokenState::getAmount))
                        ).forEach((key, value)->list.add(new TokenBalanceSummaryModel(key, value)));
                result.setData(list);
            }
        } catch (Exception e) {
            log.error("====================》com.ehkd.blockchain.service.TokenSearchService.searchAllTokenSummary异常：", e);
            result = CordaResult.error(e.getMessage());
        }
        return result;
    }

    /**
     * 获取所有机构发行token，根据token name汇总余额
     * @return
     */
    public CordaResult<List<VoucherBalanceSummaryModel>> searchAllVoucherSummary() {
        CordaResult result = CordaResult.success(null);
        try{
            List<TokenState> data = cordaService.searchTokenList(null, null, 1);
            if(data != null&&data.size()>0) {
                List<TokenBalanceSummaryModel> list = new ArrayList<>();
                data.stream().sorted(Comparator.comparing(TokenState::getName))
                        .collect(Collectors.groupingBy(TokenState::getName, LinkedHashMap::new, Collectors.toList()))
                        .forEach((key, value)->{
                            VoucherBalanceSummaryModel vbsm = new VoucherBalanceSummaryModel();
                            vbsm.setTokenName(key);
                            vbsm.setAmount(value.stream().mapToLong(TokenState::getAmount).sum());
                            list.add(vbsm);
                        });
                result.setData(list);
            }
        } catch (Exception e) {
            log.error("====================》com.ehkd.blockchain.service.TokenSearchService.searchAllVoucherSummary 异常：", e);
            result = CordaResult.error(e.getMessage());
        }
        return result;
    }

    /**
     * 获取 tokenId 信息
     * @param tokenId
     * @return
     */
    public CordaResult<TokenDetailModel> searchTokenOne(String userId, String tokenId) {
        if(StringUtils.isNotBlank(tokenId)) {
            try{
                TokenState tokenState = cordaService.searchTokenOne(userId, tokenId);
                if(tokenState != null) {
                    return CordaResult.success(new TokenDetailModel(tokenState));
                }
            } catch (Exception e) {
                log.error("====================》com.ehkd.blockchain.service.TokenSearchService.searchTokenOne 异常：", e);
                return CordaResult.error(e.getMessage());
            }
        }
        return null;
    }

    /**
     * 获取用户 userId 名下的所有个人发行token, 按tokenType汇总
     * @param userId
     * @return
     */
    public CordaResult<List<TokenBalanceSummaryModel>> searchTokenSummaryByUser(String userId) {
        CordaResult result = CordaResult.success(null);
        try{
            if(StringUtils.isNotBlank(userId)) {
                Integer isVoucher = 0;
                List<TokenState> data = cordaService.searchTokenList(userId, null, isVoucher);
                if(data != null&&data.size()>0) {
                    List<TokenBalanceSummaryModel> list = new ArrayList<>();
                    data.stream().sorted(Comparator.comparing(TokenState::getTokenType))
                            .collect(Collectors.groupingBy(
                                    TokenState::getTokenType
                                    , LinkedHashMap::new
                                    , Collectors.summingLong(TokenState::getAmount))
                            ).forEach((key, value)->list.add(new TokenBalanceSummaryModel(key, value)));
                    result.setData(list);
                }
            }
        } catch (Exception e) {
            log.error("====================》com.ehkd.blockchain.service.TokenSearchService.searchTokenSummaryByUser 异常：", e);
            result = CordaResult.error(e.getMessage());
        }
        return result;
    }

    /**
     * 获取用户 userId 名下的所有机构发行token集合
     * @param userId
     * @param pageNumber
     * @param pageSize
     * @return
     */
    public CordaResultPage<List<TokenDetailModel>> searchVoucherSummaryByUser(String userId, Integer pageNumber, Integer pageSize) {
        CordaResultPage resultPage = CordaResultPage.success(0L, pageNumber, pageSize, null);
        try {
            if(StringUtils.isNotBlank(userId)) {
                Integer isVoucher = 1;
                List<TokenState> data = cordaService.searchTokenList(userId, null, isVoucher);
                if(data != null&&data.size()>0) {
                    List<TokenDetailModel> list = Lists.newArrayList();
                    data.stream().sorted(Comparator.comparing(TokenState::getBatchId))
                            .collect(Collectors.groupingBy(TokenState::getBatchId, LinkedHashMap::new, Collectors.toList()))
                            .forEach((key, value)->{
                                TokenDetailModel tdm = new TokenDetailModel(value.get(0));
                                tdm.setAmount(value.stream().mapToLong(TokenState::getAmount).sum());
                                list.add(tdm);
                            });
                    Long total = BigDecimal.valueOf(list.size())
                            .divide(BigDecimal.valueOf(pageSize>0?pageSize:1), BigDecimal.ROUND_UP).longValue();
                    resultPage.setTotalCount(total);
                    resultPage.setData(PageUtils.toPageList(list, pageNumber, pageSize));
                }
            }
        } catch (Exception e) {
            log.error("====================》com.ehkd.blockchain.service.TokenSearchService.searchVoucherSummaryByUser 异常：", e);
            resultPage = CordaResultPage.error(e.getMessage());
        }
        return resultPage;
    }

    /**
     * 获取用户 userId 名下可在机构 merchantId 中使用的个人token汇总
     * @param userId
     * @param merchantId
     * @return
     */
    public CordaResult<TokenBalanceSummaryModel> searchTokenByReceiver(String userId, String merchantId) {
        CordaResult<TokenBalanceSummaryModel> result = CordaResult.success(null);
        try {
            StateAndRef<MemberState> merchantInfo = cordaService.searchMemberById(merchantId);
            if(merchantInfo != null){
                String tokenType = merchantInfo.getState().getData().getIndustry();
                List<TokenState> data = cordaService.searchTokenList(userId, tokenType, 0);
                if(data != null&&data.size()>0) {
                    result.setData(new TokenBalanceSummaryModel(tokenType, data.stream().mapToLong(TokenState::getAmount).sum()));
                }
            }
        } catch (Exception e) {
            log.error("====================》com.ehkd.blockchain.service.TokenSearchService.searchTokenByReceiver 异常：", e);
            result = CordaResult.error(e.getMessage());
        }
        return result;
    }

    /**
     * 获取用户 userId 名下可在机构 merchantId 中使用的机构token 集合
     *
     * @param userId
     * @param merchantId
     * @return
     */
    public CordaResult<List<TokenDetailModel>> searchVoucherByReceiver(String userId, String merchantId) {
        CordaResult<List<TokenDetailModel>> result = CordaResult.success(null);
        try {
            StateAndRef<MemberState> merchantInfo = cordaService.searchMemberById(merchantId);
            String tokenType = merchantInfo == null?null:merchantInfo.getState().getData().getIndustry();
            //查询包含 merchanId 的 merchants数据
            List<String> merchants = getMerchantListIds(merchantId);
            //根据查询出的merchants，统计可在merchants中使用的token
            List<TokenState> data = cordaService.searchTokenList(userId, null, 1);
            if(data != null&&data.size()>0) {
                List<TokenDetailModel> list = Lists.newArrayList();
                data.stream().filter(f->{
                    if(StringUtils.isBlank(f.getTokenType())&& StringUtils.isBlank(f.getMerchantList())) {
                        return true;
                    } else if(StringUtils.isNotBlank(f.getMerchantList())&&merchants!=null&&merchants.contains(f.getMerchantList())) {
                        return true;
                    } else if(tokenType != null && tokenType.equals(f.getTokenType())) {
                        return true;
                    }
                    return false;
                }).sorted(Comparator.comparing(TokenState::getBatchId))
                    .collect(Collectors.groupingBy(TokenState::getBatchId, LinkedHashMap::new, Collectors.toList()))
                    .forEach((key, value)->{
                        TokenDetailModel tdm = new TokenDetailModel(value.get(0));
                        tdm.setAmount(value.stream().mapToLong(TokenState::getAmount).sum());
                        list.add(tdm);
                    });
                result.setData(list);
            }
        } catch (Exception e) {
            log.error("====================》com.ehkd.blockchain.service.TokenSearchService.searchVoucherByReceiver 异常：", e);
            result.setCode(CordaResultCodeEnum.ERROR.getCode());
            result.setMessage(e.getMessage());
        }
        return result;
    }

    private List<String> getMerchantListIds(String merchantId) {
        List<String> list = Lists.newArrayList();
        long totalResults = 0L;
        if(StringUtils.isNotBlank(merchantId)) {
            Integer pageNumber=1, pageSize = 200;
            do {
                Vault.Page<MerchantListState> data = cordaService.getMerchantListIds(merchantId, pageNumber, pageSize);
                if(data != null) {
                    totalResults = data.getTotalStatesAvailable();
                    List<StateAndRef<MerchantListState>> states = data.getStates();
                    if(states!=null&&states.size()>0) {
                        list.addAll(states.stream().map(m->m.getState().getData().getMerchantListId()).collect(Collectors.toList()));
                    }
                }
                pageNumber++;
            } while ((pageSize * (pageNumber - 1) <= totalResults));
        }
        return list;
    }

}
