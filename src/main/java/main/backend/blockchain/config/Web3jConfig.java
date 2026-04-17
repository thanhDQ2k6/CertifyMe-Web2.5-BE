package main.backend.blockchain.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.web3j.crypto.Credentials;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.http.HttpService;

@Configuration
public class Web3jConfig {

  @Value("${app.blockchain.rpc-url}")
  private String rpcUrl;

  @Value("${app.blockchain.private-key}")
  private String privateKey;

  @Bean
  public Web3j web3j() {
    return Web3j.build(new HttpService(rpcUrl));
  }

  @Bean
  public Credentials credentials() {
    return Credentials.create(privateKey);
  }
}
