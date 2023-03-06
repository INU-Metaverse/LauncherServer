package kr.goldenmine.inuminecraftlauncher.login;

import kr.goldenmine.inuminecraftlauncher.file.FileController;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class MicrosoftKeyService {
    private static final Logger logger = LoggerFactory.getLogger(FileController.class);

    private MicrosoftKey primary;

    private final MicrosoftKeyRepository microsoftKeyRepository;

    public MicrosoftKeyService(MicrosoftKeyRepository microsoftKeyRepository) {
        this.microsoftKeyRepository = microsoftKeyRepository;
    }

    public MicrosoftKey getPrimary() {
        try {
            if (primary == null) {
                primary = microsoftKeyRepository.findAll().get(0);
            }
        } catch(Exception ex) {
            logger.error(ex.getMessage(), ex);
        }
        return primary;
    }
}