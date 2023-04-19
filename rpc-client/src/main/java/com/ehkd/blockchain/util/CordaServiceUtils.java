package com.ehkd.blockchain.util;

import com.ehkd.blockchain.sdk.CordaConfig;
import com.ehkd.blockchain.sdk.CordaService;
import com.ehkd.blockchain.service.*;

import java.nio.file.Path;

public class CordaServiceUtils {

    private CordaConfig config = new CordaConfig();

    private CordaService cordaService = new CordaService(config);

    private CordaServiceUtils(){}

    public static CordaServiceUtils Builder() {
        return new CordaServiceUtils();
    }

    public CordaServiceUtils host(String host) {
        this.config.setHost(host);
        return this;
    }

    public CordaServiceUtils port(String port) {
        this.config.setPort(port);
        return this;
    }

    public CordaServiceUtils username(String username) {
        this.config.setUsername(username);
        return this;
    }

    public CordaServiceUtils password(String password) {
        this.config.setPassword(password);
        return this;
    }

    public CordaServiceUtils usetls(boolean usetls) {
        this.config.setUsetls(usetls);
        return this;
    }

    public CordaServiceUtils certsPath(Path certsPath) {
        this.config.setCertsPath(certsPath);
        return this;
    }

    public CordaServiceUtils certsPass(String certsPass) {
        this.config.setCertsPass(certsPass);
        return this;
    }

    public MemberService getMemberService() {
        return new MemberService(cordaService);
    }

    public MerchantListService getMerchantListService() {
        return new MerchantListService(cordaService);
    }

    public MintService getMintService() {
        return new MintService(cordaService);
    }

    public ReceiverGroupService getReceiverGroupService() {
        return new ReceiverGroupService(cordaService);
    }

    public SendService getSendService() {
        return new SendService(cordaService);
    }

    public TokenSearchService getTokenSearchService() {
        return new TokenSearchService(cordaService);
    }

}
