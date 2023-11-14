package kr.goldenmine.inuminecraftlauncher.login;

import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import kr.goldenmine.inuminecraftlauncher.launcher.DefaultLauncherDirectories;
import kr.goldenmine.launchercore.UserAdministrator;
import kr.goldenmine.launchercore.util.LoopUtil;
import lombok.extern.slf4j.Slf4j;
import net.technicpack.minecraftcore.microsoft.auth.MicrosoftUser;
import org.apache.commons.io.FileUtils;
import org.openqa.selenium.By;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
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

    // 계정을 관리하는 쓰레드의 총 구현
    @Override
    public void run() {
        int count = 0;
        while (!stop) {
            try {
                // 25번마다 한번씩 마인크래프트 공홈 접속해준다.
                if(count % 25 == 0) {
                    count = 0;
                    tryAllLoginMinecraft();
                }
                count++;

                // OAuth 토큰을 얻어낸다.
                long start = System.currentTimeMillis();
                tryAllLoginOAuth();
                long time = (System.currentTimeMillis() - start);

                log.info(time + " ms is used for accessing or refreshing all accounts.");

                // 일정 기간 sleep해준다.
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

    private void tryAllLoginMinecraft() {
        // https://sisu.xboxlive.com/connect/XboxLive/?state=login&cobrandId=8058f65d-ce06-4c30-9559-473c9275a65d&tid=896928775&ru=https%3A%2F%2Fwww.minecraft.net%2Fen-us%2Flogin&aid=1142970254
        String url = "https://sisu.xboxlive.com/connect/XboxLive/?state=login&cobrandId=8058f65d-ce06-4c30-9559-473c9275a65d&tid=896928775&ru=https%3A%2F%2Fwww.minecraft.net%2Fen-us%2Flogin&aid=1142970254";

        List<MicrosoftAccount> list = microsoftAccountService.list();
        log.info("total accounts: " + list.size());

        for (MicrosoftAccount microsoftAccount : list) {
            ChromeDriver driver = getChromeDriver();

            String id = microsoftAccount.getEmail();
            String password = microsoftAccount.getPassword();

            try {
                // 마이크로소프트로 로그인
                loginMicrosoft(driver, url, id, password);

                // 마인크래프트 사이트로 이동한 이후일듯
                Optional<WebElement> minecraftSiteCheck = LoopUtil.waitWhile(() -> driver.findElements(
                        By.className("game-title"))
                        .stream()
                        .filter(it -> it.getText().toLowerCase().contains("minecraft"))
                        .findFirst()
                    , 1000L, 20);

                log.info("find: " + id + ", " + minecraftSiteCheck.isPresent());
            } catch (InterruptedException | IOException e) {
                log.error(e.getMessage(), e);
            }

            driver.quit();
            // 각 계정간 30초 텀
            try {
                Thread.sleep(30000L);
            } catch (InterruptedException e) {
                log.error(e.getMessage(), e);
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

    private void tryAllLoginOAuth() {
        List<MicrosoftAccount> list = microsoftAccountService.list();
        log.info("total accounts: " + list.size());

        for (MicrosoftAccount microsoftAccount : list) {
            try {
                log.info("try to login " + microsoftAccount.getEmail() + ", " + Math.max(microsoftAccount.getTokenExpire() - System.currentTimeMillis(), 0) / 1000 + "s remaining...");

                if (microsoftAccount.checkWhetherRefreshNeeded()) {
                    automatic.setAccount(microsoftAccount.getEmail(), microsoftAccount.getPassword());
                    try {
                        MicrosoftUser user = loginRepeat(userAdministrator, automatic, microsoftAccount.getMinecraftUsername(), 5);

                        microsoftAccount.initMicrosoftUser(user);

                        log.info("logged in " + microsoftAccount.getEmail() + ", " + microsoftAccount.getAccessToken() + ", " + microsoftAccount.getTokenExpire());
                    } catch (LoginException e) {
                        log.error(e.getMessage(), e);
//                        e.printStackTrace();
                        microsoftAccount.setAccessToken(null); // 로그인 실패시 access token을 null 처리
                    }
                    microsoftAccountService.save(microsoftAccount);
                    // 각 계정간 30초 텀
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

        @Override
        public void browse(String url) throws IOException {
            synchronized (this) {
                if (running) throw new RuntimeException("browse(url) is already running");
                running = true;
            }

            log.info("url: " + url);

            ChromeDriver driver = getChromeDriver();

            try {
                loginMicrosoft(driver, url, id, password);
                saveCurrentSiteResult(driver);
                Thread.sleep(10000L);
            } catch(IOException ex) {
                throw ex;
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

    public static void loginMicrosoft(ChromeDriver driver, String url, String id, String password) throws InterruptedException, IOException {
        // load login html
        driver.get(url);
        Thread.sleep(1000L);

        Runtime.getRuntime().addShutdownHook(new Thread(driver::quit));

        WebElement idElement = LoopUtil.waitWhile(() -> driver.findElements(By.tagName("input")).stream().filter(it -> "email".equals(it.getAttribute("type"))).findFirst(), 1000L, -1).get();

        Optional<WebElement> optionalSubmitElement = driver.findElements(By.tagName("input")).stream().filter(it -> "submit".equals(it.getAttribute("type"))).findFirst();
        WebElement submitElement = optionalSubmitElement.get();

        idElement.sendKeys(id);
        log.info("login id " + id);
        Thread.sleep(1000L);
        submitElement.click();

        WebElement passwordElement = LoopUtil.waitWhile(() -> driver.findElements(By.tagName("input")).stream().filter(it -> "password".equals(it.getAttribute("type"))).findFirst(), 1000L, -1).get();
        submitElement = driver.findElements(By.tagName("input")).stream().filter(it -> "submit".equals(it.getAttribute("type"))).findFirst().get();

        passwordElement.sendKeys(password);
        log.info("login password");
        Thread.sleep(1000L);
        submitElement.click();

//                Thread.sleep(5000L);

        Optional<WebElement> terms = LoopUtil.waitWhile(() ->
                        driver.findElements(By.tagName("input"))
                                .stream()
                                .filter(
                                        it -> "submit".equals(it.getAttribute("type")) &&
                                                ("다음".equals(it.getAttribute("value")) || "Next".equalsIgnoreCase(it.getAttribute("value")))
                                ).findFirst(),
                1000L, 5);

        terms.ifPresent(WebElement::click);

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
        } else {
            throw new IOException("failed to click");
        }
        log.info("ended");
        Thread.sleep(10000L);
    }

    public static void saveCurrentSiteResult(ChromeDriver driver) throws IOException {
        File srcFile = ((TakesScreenshot)driver).getScreenshotAs(OutputType.FILE);
        FileUtils.copyFile(srcFile, new File("headless.png"));
    }

    public static ChromeDriver getChromeDriver() {
        ChromeOptions chromeOptions = new ChromeOptions();
//            chromeOptions.addArguments("--start-maximized");
        chromeOptions.addArguments("--remote-allow-origins=*");
        chromeOptions.addArguments("--headless=new");
//            chromeOptions.addArguments("--headless");
//            chromeOptions.addArguments("--disable-gpu");
//            chromeOptions.addArguments("--auth-server-whitelist=\"localhost:20200\"");
//            chromeOptions.addArguments("--headless");
//            chromeOptions.addArguments("no-sandbox");
        chromeOptions.addArguments("--window-size=1920,1080");
        return new ChromeDriver(chromeOptions);
    }

    class LoginException extends RuntimeException {
        public LoginException(String message) {
            super(message);
        }
    }
}
