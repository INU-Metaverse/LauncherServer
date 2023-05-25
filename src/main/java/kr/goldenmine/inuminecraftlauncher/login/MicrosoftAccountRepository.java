package kr.goldenmine.inuminecraftlauncher.login;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MicrosoftAccountRepository extends JpaRepository<MicrosoftAccount,Integer> {
    @Query("SELECT account FROM MicrosoftAccount account WHERE account.minecraftUsername = :username")
    List<MicrosoftAccount> getAccountFromUserName(@Param("username") String userName);

    @Query("SELECT COUNT(account) FROM MicrosoftAccount account")
    int countTotalAccounts();

    @Query("SELECT COUNT(account) FROM MicrosoftAccount account WHERE account.ipAddress = :current_ip AND account.serverBorrowed = 1")
    int countJoinedIps(@Param("current_ip") String ip);

    @Query("SELECT COUNT(account) FROM MicrosoftAccount account WHERE account.serverBorrowed = 0 AND account.accessToken IS NOT NULL")
    int countAvailableAccounts();

    @Query("SELECT account FROM MicrosoftAccount account WHERE account.serverBorrowed = 0 AND account.accessToken IS NOT NULL")
    Page<MicrosoftAccount> getAvailableAccounts(Pageable pageable);



    /*
      @Query(value = "SELECT * FROM USERS WHERE LASTNAME = ?1",
    countQuery = "SELECT count(*) FROM USERS WHERE LASTNAME = ?1",
    nativeQuery = true)
  Page<User> findByLastname(String lastname, Pageable pageable);
     */
}
