package kr.goldenmine.inuminecraftlauncher.login;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

@Service
public class MicrosoftAccountService {
    private final MicrosoftAccountRepository microsoftAccountRepository;

    private final List<Object> lockKeys = new ArrayList<>();
    private final Object keyCreateLock = new Object();

    public MicrosoftAccountService(MicrosoftAccountRepository microsoftAccountRepository) {
        this.microsoftAccountRepository = microsoftAccountRepository;
    }

    public MicrosoftAccount save(MicrosoftAccount user) {
        return microsoftAccountRepository.save(user);
    }

    public List<MicrosoftAccount> list() {
        return microsoftAccountRepository.findAll();
    }

    public void flush() {
        microsoftAccountRepository.flush();
    }

    public Object getLockKey(int id) {
        if(id >= lockKeys.size()) {
            synchronized (keyCreateLock) {
                while(id >= lockKeys.size()) {
                    lockKeys.add(new Object());
                }
            }
        }

        return lockKeys.get(id);
    }

    public Optional<MicrosoftAccount> selectOneAccount() {
        long currentTime = System.currentTimeMillis();

        // 조건에 맞는 게시글의 개수를 가져온다.
        int qty = microsoftAccountRepository.countAvailableAccounts(currentTime);
        // 가져온 개수 중 랜덤한 하나의 인덱스를 뽑는다.
        int idx = (int)(Math.random() * qty);
        // 페이징하여 하나만 추출해낸다.
        Page<MicrosoftAccount> postPage = microsoftAccountRepository.getAvailableAccounts(currentTime, PageRequest.of(idx, 1));

        if (postPage.hasContent()) {
            MicrosoftAccount post = postPage.getContent().get(0);
            return Optional.of(post);
        }

        return Optional.empty();
    }

    public void synchronize(MicrosoftAccount account, Consumer<MicrosoftAccount> lambda) {
        Object lockKey = getLockKey(account.getId());

        synchronized (lockKey) {
            lambda.accept(account);
        }
    }

    public int countAllAccounts() {
        return microsoftAccountRepository.countTotalAccounts();
    }

    public int countAvailableAccounts() {
        long currentTime = System.currentTimeMillis();
        int qty = microsoftAccountRepository.countAvailableAccounts(currentTime);

        return qty;
    }

    public List<MicrosoftAccount> getAccountFromUsername(String username) {
        return microsoftAccountRepository.getAccountFromUserName(username);
    }
}