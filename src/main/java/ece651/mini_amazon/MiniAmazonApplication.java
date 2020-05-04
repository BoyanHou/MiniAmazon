package ece651.mini_amazon;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

@SpringBootApplication
public class MiniAmazonApplication {

    public static void main(String[] args) {
        SpringApplication.run(MiniAmazonApplication.class, args);
    }

}
