package com.ehkd.blockchain.sdk;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

import java.nio.file.Path;
import java.nio.file.Paths;

@Setter
@Getter
@EnableConfigurationProperties(CordaConfig.class)
@ConfigurationProperties(prefix = "config.rpc")
public class CordaConfig {

    String host = "localhost";

    String port = "10006";

    String username = "user1";

    String password = "test";

    boolean usetls = false;

    Path certsPath = Paths.get("reccerts");

    String certsPass = "abcabc";

}
