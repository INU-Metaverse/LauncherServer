package kr.goldenmine.inuminecraftlauncher;

import kr.goldenmine.inuminecraftlauncher.login.MicrosoftAccountService;
import kr.goldenmine.inuminecraftlauncher.login.MicrosoftAccount;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Optional;

@SpringBootTest
@RunWith(SpringRunner.class)
public class MicrosoftDBTest {

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
    public void testSelectOne() {
        Optional<MicrosoftAccount> accountOptional = microsoftAccountService.selectOneAccount();

        System.out.println(accountOptional.isPresent());

        accountOptional.ifPresent(System.out::println);
    }
}
