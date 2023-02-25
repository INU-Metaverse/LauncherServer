package kr.goldenmine.inuminecraftlauncher.login;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MicrosoftKeyRepository extends JpaRepository<MicrosoftKey,Integer> {

}
