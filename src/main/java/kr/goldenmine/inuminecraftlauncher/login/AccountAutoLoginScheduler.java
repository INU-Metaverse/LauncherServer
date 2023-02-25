package kr.goldenmine.inuminecraftlauncher.login;

import kr.goldenmine.inuminecraftlauncher.login.impl.AccountException;
import kr.goldenmine.inuminecraftlauncher.request.MicrosoftServiceImpl;
import kr.goldenmine.inuminecraftlauncher.request.models.MicrosoftTokenResponse;
import kr.goldenmine.inuminecraftlauncher.request.models.minecraft.MinecraftProfileResponse;
import kr.goldenmine.inuminecraftlauncher.request.models.xbox.XBoxXstsResponse;
import kr.goldenmine.inuminecraftlauncher.util.LoopUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
public class AccountAutoLoginScheduler extends Thread {
    private final MicrosoftAccountService microsoftAccountService;
    private final MicrosoftKeyService microsoftKeyService;

    private final long sleepInMS;
    private boolean stop = false;

    @Autowired
    public AccountAutoLoginScheduler(MicrosoftAccountService microsoftAccountService, MicrosoftKeyService microsoftKeyService) {
        this.microsoftAccountService = microsoftAccountService;
        this.microsoftKeyService = microsoftKeyService;

        sleepInMS = 900 * 1000L; // 토큰 expire의 1/4

        start();
        log.info("AutoLoginScheduler is started.");
    }

    public void stopSafely() {
        stop = true;
        interrupt();
    }

    @Override
    public void run() {
        while (!stop) {
            try {
                long start = System.currentTimeMillis();
                tryAllLogin();
                long time = (System.currentTimeMillis() - start);

                log.info(time + " ms is used for accessing or refreshing all accounts.");

                Thread.sleep(Math.max(sleepInMS - time, 1));
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

    private MicrosoftTokenResponse getMicrosoftTokenResponse(MicrosoftAccount microsoftAccount) throws InterruptedException, AccountException {
        MicrosoftTokenResponse response;
        if (microsoftAccount.getRecentRefreshToken() == null) {
            Optional<MicrosoftTokenResponse> responseOptional = LoopUtil.waitWhile(() -> {
                try {
                    MicrosoftTokenResponse result = MicrosoftServiceImpl.firstLogin(microsoftAccount.getEmail(), microsoftAccount.getPassword());
                    if (result != null) {
                        return Optional.of(result);
                    }
                } catch (Exception ex) {
                    log.error(ex.getMessage(), ex);
                }
                return Optional.empty();
            }, 1000L, 5);

            if(responseOptional.isPresent()) {
                response = responseOptional.get();
            } else {
                throw new AccountException("failed to get microsoft token response when refresh token is none");
            }
        } else {
            Optional<MicrosoftTokenResponse> responseOptional = LoopUtil.waitWhile(() -> {
                try {
                    return Optional.of(MicrosoftServiceImpl.refresh(microsoftAccount.getRecentRefreshToken()));
                } catch (Exception ex) {
                    log.error(ex.getMessage(), ex);
                    return Optional.empty();
                }
            }, 1000L, 5);

            if(responseOptional.isPresent()) {
                response = responseOptional.get();
            } else {
                throw new AccountException("failed to get microsoft token response when refresh token exists");
            }
        }

        return response;
    }

    private XBoxXstsResponse getXBoxResponse(MicrosoftTokenResponse response) throws InterruptedException, AccountException {
        Optional<XBoxXstsResponse> xBoxResponse = LoopUtil.waitWhile(() -> {
            try {
                return Optional.of(MicrosoftServiceImpl.loginXbox(response.getAccessToken()));
            } catch (IOException e) {
                log.error(e.getMessage(), e);
                return Optional.empty();
            }
        }, 1000L, 5);

        if(xBoxResponse.isPresent()) {
            return xBoxResponse.get();
        } else {
            throw new AccountException("failed to get xbox token response");
        }
    }

    private MinecraftProfileResponse getMinecraftProfileResponse(XBoxXstsResponse xBoxXstsResponse) throws InterruptedException, AccountException {

        Optional<MinecraftProfileResponse> minecraftProfileResponse = LoopUtil.waitWhile(() -> {
            try {
                return Optional.of(MicrosoftServiceImpl.getMinecraftProfile(xBoxXstsResponse.getToken(), xBoxXstsResponse.getPreviousUhs()));
            } catch (IOException e) {
                log.error(e.getMessage(), e);
                return Optional.empty();
            }
        }, 1000L, 5);

        if(minecraftProfileResponse.isPresent()) {
            return minecraftProfileResponse.get();
        } else {
            throw new AccountException("failed to get xbox token response");
        }
    }

    private boolean checkWhetherRefreshNeeded(MicrosoftAccount microsoftAccount) {
        final long INNER_TIME = System.currentTimeMillis();
        boolean borrowedButNotUsed = microsoftAccount.getServerBorrowed() == 1
                && microsoftAccount.getServerJoined() == 0
                && INNER_TIME >= microsoftAccount.getServerBorrowedExpire();

        boolean accessTimeExpired = INNER_TIME + sleepInMS * 1.5 >= microsoftAccount.getTokenExpire();

        boolean quitted = microsoftAccount.getServerQuitted() == 1;

        return borrowedButNotUsed || (accessTimeExpired && quitted);
    }

    private void tryAllLogin() {
        List<MicrosoftAccount> list = microsoftAccountService.list();
        MicrosoftKey primary = microsoftKeyService.getPrimary();

        log.info("client id: " + primary.getClientId());
        log.info("client secret: " + primary.getClientSecret());
        log.info("current time: " + System.currentTimeMillis());

        MicrosoftServiceImpl.clientId = primary.getClientId();
        MicrosoftServiceImpl.clientSecret = primary.getClientSecret();
        MicrosoftServiceImpl.state = UUID.randomUUID().toString(); // random state for security

        log.info("total accounts: " + list.size());

        for (MicrosoftAccount microsoftAccount : list) {
            try {
                final long INNER_TIME = System.currentTimeMillis();

                log.info("trying to login " + microsoftAccount.getEmail() + ", " + (microsoftAccount.getTokenExpire() - INNER_TIME) / 1000 + "s remaining...");

                if (checkWhetherRefreshNeeded(microsoftAccount)) {
                    MicrosoftTokenResponse microsoftTokenResponse = getMicrosoftTokenResponse(microsoftAccount);
                    XBoxXstsResponse xBoxResponse = getXBoxResponse(microsoftTokenResponse);
                    MinecraftProfileResponse minecraftProfileResponse = getMinecraftProfileResponse(xBoxResponse);

                    microsoftAccount.setServerQuitted(1);
                    microsoftAccount.setServerJoined(0);
                    microsoftAccount.setServerBorrowedExpire(0);
                    microsoftAccount.setServerBorrowed(0);
                    microsoftAccount.setTokenExpire(INNER_TIME + microsoftTokenResponse.getExpiresIn() * 1000L);
                    microsoftAccount.setRecentAccessToken(microsoftTokenResponse.getAccessToken());
                    microsoftAccount.setRecentRefreshToken(microsoftTokenResponse.getRefreshToken());
                    microsoftAccount.setRecentProfileToken(xBoxResponse.getToken());
                    microsoftAccount.setMinecraftUsername(minecraftProfileResponse.getName());
                    microsoftAccount.setMinecraftUuid(minecraftProfileResponse.getId().toString());

                    microsoftAccountService.save(microsoftAccount);

                    log.info("logged " + microsoftAccount.getEmail() + ", " + microsoftAccount.getRecentProfileToken() + ", " + microsoftTokenResponse.getExpiresIn());
                } else {
                    log.info("skipped " + microsoftAccount.getEmail());
                }

                Thread.sleep(sleepInMS / 1000L);
            } catch (Exception e) {
                log.error(e.getMessage(), e);
            }
        }
        microsoftAccountService.flush();
    }
}
