package kr.goldenmine.inuminecraftlauncher;

import io.github.bonigarcia.wdm.WebDriverManager;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ConfigurableApplicationContext;

import java.io.IOException;
import java.util.concurrent.ExecutionException;

@SpringBootApplication
@EnableConfigurationProperties({ FileStorageProperties.class })
public class Main {
    /*

    microsoft oauth
    https://learn.microsoft.com/en-us/azure/active-directory/develop/v2-oauth2-auth-code-flow

    minecraft login
    https://mojang-api-docs.netlify.app/authentication/msa.html

    이제 마인크래프트 실행만 할 수 있으면 되는데...

    (old) minecraft launch
    https://stackoverflow.com/questions/14531917/launch-minecraft-from-command-line-username-and-password-as-prefix
     */

    // --spring.config.location=classpath:/default.properties,classpath:/override.properties
    public static void main(String[] args) throws IOException, ExecutionException, InterruptedException {
//        MicrosoftServiceImpl.tryAllLogin();
        WebDriverManager.chromedriver().setup();

        SpringApplicationBuilder builder = new SpringApplicationBuilder(CoreMain.class);
        builder.headless(false);
        ConfigurableApplicationContext context = builder.run(args);
    }
}