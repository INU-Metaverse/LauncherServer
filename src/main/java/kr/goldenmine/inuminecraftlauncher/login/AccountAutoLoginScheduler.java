package kr.goldenmine.inuminecraftlauncher.login;

import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import kr.goldenmine.inuminecraftlauncher.launcher.DefaultLauncherDirectories;
import kr.goldenmine.launchercore.UserAdministrator;
import kr.goldenmine.launchercore.util.LoopUtil;
import lombok.extern.slf4j.Slf4j;
import net.technicpack.minecraftcore.microsoft.auth.MicrosoftUser;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
public class AccountAutoLoginScheduler extends Thread {
    static {
        deleteAccountFile();
    }

    private final MicrosoftAccountService microsoftAccountService;
    private final MicrosoftKeyService microsoftKeyService;
    private final DefaultLauncherDirectories directories = new DefaultLauncherDirectories(new File("inulauncher"));
    private final UserAdministrator userAdministrator = new UserAdministrator(directories);
    private final BrowserAutomatic automatic = new BrowserAutomatic();

    private boolean stop = false;

    @Autowired
    public AccountAutoLoginScheduler(MicrosoftAccountService microsoftAccountService, MicrosoftKeyService microsoftKeyService) {


        this.microsoftAccountService = microsoftAccountService;
        this.microsoftKeyService = microsoftKeyService;

        start();
        log.info("AutoLoginScheduler is started.");
    }

    public void stopSafely() {
        stop = true;
        interrupt();
    }

    public static void deleteAccountFile() {
        File file = new File("inulauncher/oauth");
        deleteDirectory(file);

        File file2 = new File("inulauncher/users.json");
        if (file2.exists()) file2.delete();
    }

    static boolean deleteDirectory(File directoryToBeDeleted) {
        File[] allContents = directoryToBeDeleted.listFiles();
        if (allContents != null) {
            for (File file : allContents) {
                deleteDirectory(file);
            }
        }
        return directoryToBeDeleted.delete();
    }

    @Override
    public void run() {
        try {
            Thread.sleep(30000L); // 30초 뒤 동작
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        while (!stop) {
            try {
                long start = System.currentTimeMillis();
                tryAllLogin();
                long time = (System.currentTimeMillis() - start);

                log.info(time + " ms is used for accessing or refreshing all accounts.");

                Thread.sleep(Math.max(MicrosoftAccount.SLEEP_IN_MS - time, 1));
            } catch (InterruptedException ex) {
                log.warn(ex.getMessage());
            } catch (Exception ex) {
                log.error(ex.getMessage(), ex);

                try {
                    Thread.sleep(10000L);
                } catch (InterruptedException ex2) {
                    log.error(ex2.getMessage(), ex2);
                }
            }
        }
    }

    private MicrosoftUser loginRepeat(UserAdministrator administrator, BrowserAutomatic browser, String username, int repeat) {
        for (int i = 0; i < repeat; i++) {
            try {
                return administrator.login(username, browser);
            } catch (Exception ex) {
                log.error(ex.getMessage(), ex);
            }

            try {
                Thread.sleep(20000L);
            } catch (InterruptedException ex) {
                log.error(ex.getMessage(), ex);
            }
        }

        throw new LoginException("login failed severaly");
    }

    private void tryAllLogin() {
        List<MicrosoftAccount> list = microsoftAccountService.list();
        log.info("total accounts: " + list.size());

        for (MicrosoftAccount microsoftAccount : list) {
            try {
                log.info("try to login " + microsoftAccount.getEmail() + ", " + Math.max(microsoftAccount.getTokenExpire() - System.currentTimeMillis(), 0) / 1000 + "s remaining...");

                if (microsoftAccount.checkWhetherRefreshNeeded()) {
                    automatic.setAccount(microsoftAccount.getEmail(), microsoftAccount.getPassword());
                    try {
                        MicrosoftUser user = loginRepeat(userAdministrator, automatic, microsoftAccount.getMinecraftUsername(), 10);

                        microsoftAccount.initMicrosoftUser(user);

                        log.info("logged in " + microsoftAccount.getEmail() + ", " + microsoftAccount.getAccessToken() + ", " + microsoftAccount.getTokenExpire());
                    } catch (LoginException e) {
                        log.error(e.getMessage(), e);
//                        e.printStackTrace();
                        microsoftAccount.setAccessToken(null); // 로그인 실패시 access token을 null 처리
                    }
                    microsoftAccountService.save(microsoftAccount);
                    // 각 계정간 20초 텀
                    Thread.sleep(30000L);
                } else {
                    log.info("skipped " + microsoftAccount.getEmail());
                    // 스킵은 뭐 그냥 0.1초텀
                    Thread.sleep(100L);
                }
            } catch (Exception e) {
                log.error(e.getMessage(), e);
            }
        }
        microsoftAccountService.flush();
    }

    public static class BrowserAutomatic implements AuthorizationCodeInstalledApp.Browser {
        private String id;
        private String password;

        private boolean running = false;
//        private ChromeDriver driver;

        public void setAccount(String id, String password) {
            this.id = id;
            this.password = password;
        }

        public static ChromeDriver getChromeDriver() {
            ChromeOptions chromeOptions = new ChromeOptions();
            chromeOptions.addArguments("--start-maximized");
            chromeOptions.addArguments("--remote-allow-origins=*");
            chromeOptions.addArguments("--headless=new");
//            chromeOptions.addArguments("--headless");
            chromeOptions.addArguments("--window-size=1920,1080");
            return new ChromeDriver(chromeOptions);
        }

        @Override
        public void browse(String url) throws IOException {
            synchronized (this) {
                if (running) throw new RuntimeException("browse(url) is already running");
                running = true;
            }

            log.info("url: " + url);

            ChromeDriver driver = getChromeDriver();

            try {
                // load login html
                driver.get(url);
                Thread.sleep(1000L);

                Runtime.getRuntime().addShutdownHook(new Thread(driver::quit));

                WebElement idElement = LoopUtil.waitWhile(() -> driver.findElements(By.tagName("input")).stream().filter(it -> "email".equals(it.getAttribute("type"))).findFirst(), 1000L, -1).get();

                Optional<WebElement> optionalSubmitElement = driver.findElements(By.tagName("input")).stream().filter(it -> "submit".equals(it.getAttribute("type"))).findFirst();
                WebElement submitElement = optionalSubmitElement.get();

                idElement.sendKeys(id);
                log.info("login id");
                Thread.sleep(500L);
                submitElement.click();

                WebElement passwordElement = LoopUtil.waitWhile(() -> driver.findElements(By.tagName("input")).stream().filter(it -> "password".equals(it.getAttribute("type"))).findFirst(), 1000L, -1).get();
                submitElement = driver.findElements(By.tagName("input")).stream().filter(it -> "submit".equals(it.getAttribute("type"))).findFirst().get();

                passwordElement.sendKeys(password);
                log.info("login password");
                Thread.sleep(500L);
                submitElement.click();

                Optional<WebElement> find = LoopUtil.waitWhile(() ->
                        driver.findElements(By.tagName("input"))
                                .stream()
                                .filter(
                                    it -> "button".equals(it.getAttribute("type")) &&
                                        ("아니요".equals(it.getAttribute("value")) || "No".equals(it.getAttribute("value")))
                                ).findFirst(),
                        1000L, 10);

                if (find.isPresent()) {
                    WebElement nextButton = driver.findElements(By.tagName("input"))
                            .stream()
                            .filter(it -> "button".equals(it.getAttribute("type")) &&
                                    ("아니요".equals(it.getAttribute("value")) || "No".equals(it.getAttribute("value")))
                    ).findFirst().get();
                    nextButton.click();
                }
                log.info("ended");
            } catch (Exception ex) {
                log.error(ex.getMessage(), ex);
            } finally {
                driver.quit();
                synchronized (this) {
                    running = false;
                }
            }
        }
    }

    class LoginException extends RuntimeException {
        public LoginException(String message) {
            super(message);
        }
    }
}
