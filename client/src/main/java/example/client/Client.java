package example.client;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.geode.boot.autoconfigure.ContinuousQueryAutoConfiguration;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication(exclude = ContinuousQueryAutoConfiguration.class)
@EnableScheduling
public class Client {

  public static void main(String[] args) {
    new SpringApplicationBuilder(Client.class)
      .build()
      .run(args);
  }
}
