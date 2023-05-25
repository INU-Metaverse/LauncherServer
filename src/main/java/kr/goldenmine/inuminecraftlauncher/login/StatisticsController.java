package kr.goldenmine.inuminecraftlauncher.login;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/statistics")
public class StatisticsController {

    private final MicrosoftAccountService microsoftAccountService;

    public StatisticsController(MicrosoftAccountService microsoftAccountService) {
        this.microsoftAccountService = microsoftAccountService;
    }

    @RequestMapping(
            value = "/join",
            method = RequestMethod.GET
    )
    public ResponseEntity<String> join(String username) {
        log.info("joined username: " + username);
        final List<MicrosoftAccount> accounts = microsoftAccountService.getAccountFromUsername(username);

        if (accounts.size() > 0) {
            final MicrosoftAccount account = accounts.get(0);

//            if(account.getServerJoined() == 0) {
            account.setServerJoined(1);
            account.setServerQuitted(0);

            microsoftAccountService.save(account);

            log.info("joined " + account.getMinecraftUsername() + " to minecraft server.");
//            } else {
//                log.warn("joined " + account.getMinecraftUsername() + ", but nothing changed.");
//            }
            return ResponseEntity.ok("");
        } else {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("");
        }
    }

    @RequestMapping(
            value = "/quit",
            method = RequestMethod.GET
    )
    public ResponseEntity<String> quit(String username) {
        log.info("quitted username: " + username);
        List<MicrosoftAccount> accounts = microsoftAccountService.getAccountFromUsername(username);

        if (accounts.size() > 0) {
            MicrosoftAccount account = accounts.get(0);

//            if (account.getServerQuitted() == 0) {
            account.setServerQuitted(1);
//                account.setServerBorrowed(0);

            microsoftAccountService.save(account);

            log.info("quitted " + account.getMinecraftUsername() + " from minecraft server.");
//            } else {
//                log.warn("quitted " + account.getMinecraftUsername() + ", but nothing changed.");
//            }

            return ResponseEntity.ok("");
        } else {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("");
        }
    }
}
