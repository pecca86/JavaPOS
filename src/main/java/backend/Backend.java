package backend;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import sharedResources.productCatalog.ProductCatalog;

@SpringBootApplication
public class Backend {

    public static void main (String[] args) {

        SpringApplication.run(Backend.class, args);

    }

    @Bean
    public CommandLineRunner run (ApplicationContext context) {
        return args -> {
            System.out.println("Initialising product catalog");
            ProductCatalog catalog = ProductCatalog.getInstance();
            catalog.fetchProducts("http://localhost:9003/rest/findByName/*");

            // HARD CODE A PRICE FOR TESTING PURPOSES!
            catalog
                    .getAllProducts()
                    .forEach((k, v) -> v.setPrice(Math.round((float) ((float) .50 + Math.random() * (100 - .50)) / 10)));

        };
    }

}
