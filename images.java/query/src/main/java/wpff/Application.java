package wpff;

// Swagger
import static springfox.documentation.builders.PathSelectors.regex;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;

import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

/**
 * Root application for query endpoint
 */
@SpringBootApplication
@EnableSwagger2
// Look for wpff packages
@ComponentScan("wpff")
public class Application {

  public static void main(String[] args) {
    SpringApplication.run(Application.class, args);
  }


  @Bean
  public Docket newsApi() {
    return new Docket(DocumentationType.SWAGGER_2)
        .apiInfo(apiInfo())
        .select()
        .paths(regex("/query.*"))
        .build();
  }
     
  private ApiInfo apiInfo() {
    return new ApiInfoBuilder()
        .title("Query endpoint")
        .description("/query endpoint for using openlibrary.org api")
        .version("2.0")
        .build();
  }


}
