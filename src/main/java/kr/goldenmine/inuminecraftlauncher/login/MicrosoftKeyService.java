package kr.goldenmine.inuminecraftlauncher.login;

import org.springframework.stereotype.Service;

@Service
public class MicrosoftKeyService {
    private MicrosoftKey primary;

    private final MicrosoftKeyRepository microsoftKeyRepository;

    public MicrosoftKeyService(MicrosoftKeyRepository microsoftKeyRepository) {
        this.microsoftKeyRepository = microsoftKeyRepository;
    }

    public MicrosoftKey getPrimary() {
        if(primary == null) {
            primary = microsoftKeyRepository.findAll().get(0);
        }
        return primary;
    }
}