package kr.goldenmine.inuminecraftlauncher.login;

import kr.goldenmine.inuminecraftlauncher.launcher.models.MinecraftAccount;
import kr.goldenmine.inuminecraftlauncher.models.ServerStatusResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Optional;

@Slf4j
@RestController
@RequestMapping("/account")
public class AccountController {
    private final MicrosoftAccountService microsoftAccountService;
    private final MicrosoftKeyService microsoftKeyService;

    public AccountController(MicrosoftAccountService microsoftAccountService, MicrosoftKeyService microsoftKeyService) {
        this.microsoftAccountService = microsoftAccountService;
        this.microsoftKeyService = microsoftKeyService;
    }

    @RequestMapping(
            value = "/random",
            method = RequestMethod.POST
    )
    public ResponseEntity<MinecraftAccount> randomAccount(
            final HttpServletRequest req,
            final HttpServletResponse res) throws Exception {
        final String remoteIp = req.getRemoteAddr();
        final int ipCount = microsoftAccountService.countJoinedIps(remoteIp);

        final Optional<MicrosoftAccount> accountOptional = microsoftAccountService.selectOneAccount();

        if (accountOptional.isPresent() && ipCount == 0) {
            final MicrosoftAccount account = accountOptional.get();

            final Object lockKey = microsoftAccountService.getLockKey(account.getId());

            if(account.getServerBorrowed() == 0) {
                synchronized (lockKey) {
                    if(account.getServerBorrowed() == 0) {
                        account.setTokenExpire(System.currentTimeMillis() + 60 * 1000L); // 곧바로 리프레쉬 되게 설정
                        account.setIpAddress(remoteIp);
                        account.setServerBorrowed(1);

                        microsoftAccountService.save(account);

                        log.info("borrowed " + account.getMinecraftUsername() + " to " + remoteIp);

                        MinecraftAccount minecraftAccount = new MinecraftAccount(
                                account.getMinecraftUsername(),
                                account.getMinecraftUuid(),
                                account.getAccessToken(),
                                "msa"
                        );

                        return ResponseEntity.ok(minecraftAccount);
                    }
                    else {
                        return ResponseEntity.status(HttpStatus.CONFLICT).body(null);
                    }
                }
            }
        }
        return ResponseEntity.status(HttpStatus.NOT_ACCEPTABLE).body(null);
    }

    @RequestMapping(
            value = "/key",
            method = RequestMethod.GET
    )
    public ResponseEntity<MicrosoftKey> getClientKey(
            final HttpServletRequest req,
            final HttpServletResponse res) {
        MicrosoftKey key = microsoftKeyService.getPrimary();

        if (key != null) {
            return ResponseEntity.ok(key);
        }
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
    }

    @RequestMapping(
            value = "/status",
            method = RequestMethod.GET
    )
    public ResponseEntity<ServerStatusResponse> status(
            final HttpServletRequest req,
            final HttpServletResponse res
    ) {
        int available = microsoftAccountService.countAvailableAccounts();
        int total = microsoftAccountService.countAllAccounts();

        log.info(available + "/" + total);

        ServerStatusResponse response = new ServerStatusResponse(available, total);

        return ResponseEntity.ok(response);
    }
}
