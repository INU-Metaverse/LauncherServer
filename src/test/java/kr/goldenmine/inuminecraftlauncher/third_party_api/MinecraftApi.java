package kr.goldenmine.inuminecraftlauncher.third_party_api;

import kr.goldenmine.inuminecraftlauncher.auth.MicrosoftSession;
import kr.goldenmine.inuminecraftlauncher.login.MicrosoftAccount;
import kr.goldenmine.inuminecraftlauncher.login.MicrosoftAccountService;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import sk.tomsik68.mclauncher.backend.MinecraftLauncherBackend;
import sk.tomsik68.mclauncher.impl.common.Platform;

import java.io.File;

@SpringBootTest
@RunWith(SpringRunner.class)
public class MinecraftApi {

    @Autowired
    private MicrosoftAccountService microsoftAccountService;

//    @Test
//    public void test() {
//        MicrosoftAccount test = new MicrosoftAccount();
//        test.setEmail("ehe123@naver.com3");
//        test.setPassword("?????????");
//        test.setRecentAccessToken("asdf");
//        microsoftAccountService.save(test);
//
//        microsoftAccountService.list().forEach(System.out::println);
//    }

    @Test
    public void test() throws Exception {
        File workingDirectory = Platform.getCurrentPlatform().getWorkingDirectory();

        System.out.println(workingDirectory.getAbsolutePath());

//        String[] authProfileNames = GlobalAuthenticationSystem.getProfileNames();
//
//        System.out.println(Arrays.toString(authProfileNames));
//
//        int selection = 0;
//
//
//        MinecraftVersion mc1165 = new MinecraftVersion(
//                "1.16.5",
//                "release",
//                "https://launchermeta.mojang.com/v1/packages/95af6e50cd04f06f65c76e4a62237504387e5480/1.16.5.json",
//                "2022-02-25T13:15:31+00:00",
//                "2021-01-14T16:05:32+00:00"
//        );

//        MicrosoftSession session = new MicrosoftSession();

        MicrosoftAccount account = microsoftAccountService.selectOneAccount().get();
        System.out.println(account);
//        MicrosoftSession session = new MicrosoftSession(account.getMinecraftUsername(), account.getRecentProfileToken(), account.getMinecraftUuid());

//        ISession loginSession = GlobalAuthenticationSystem.login(session);
        MinecraftLauncherBackend launcher = new MinecraftLauncherBackend(workingDirectory);

//        launcher.launchMinecraft(session, "1.12.2");

        /*
            {
      "id": "1.16.5",
      "type": "release",
      "url": "https://launchermeta.mojang.com/v1/packages/95af6e50cd04f06f65c76e4a62237504387e5480/1.16.5.json",
      "time": "2022-02-25T13:15:31+00:00",
      "releaseTime": "2021-01-14T16:05:32+00:00"
    },
         */
    }
}