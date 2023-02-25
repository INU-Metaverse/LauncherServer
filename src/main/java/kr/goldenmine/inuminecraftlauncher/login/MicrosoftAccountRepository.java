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

    /*
    @Query("Select re from RequiredAccountEntity re where re.debitAccNo= :debitAccNo "
          + "AND re.date<= :toDate AND re.date>= :fromDate AND re.tnxAmt tnxAmtFlag :tnxAmt ") List<RequiredAccountEntity>
          fetchAllData(@Param("debitAccNo") String debitAccNo, @Param("fromDate")
          Date fromDate, @Param("toDate") Date toDate, @Param("tnxAmt") Double tnxAmt,@Param("tnxAmtFlag") String tnxAmtFlag);
     */

    @Query("SELECT account FROM MicrosoftAccount account WHERE account.minecraftUsername = :username")
    List<MicrosoftAccount> getAccountFromUserName(@Param("username") String userName);

    @Query("SELECT COUNT(account) FROM MicrosoftAccount account")
    int countTotalAccounts();

    @Query("SELECT COUNT(account) FROM MicrosoftAccount account WHERE account.recentAccessedIp = :current_ip AND account.serverJoined = 1")
    int countJoinedIps(@Param("current_ip") String ip);

    /*
                    boolean borrowedButNotUsed = microsoftAccount.getServerBorrowed() == 1
                        && microsoftAccount.getServerJoined() == 0
                        && microsoftAccount.getServerBorrowedExpire() <= INNER_TIME;

                boolean accessTimeExpired = INNER_TIME + sleepInMS * 1.5 >= microsoftAccount.getTokenExpire();

                boolean quitted = microsoftAccount.getServerQuitted() == 1;

                                if(borrowedButNotUsed || (accessTimeExpired && quitted)) {
     */
    @Query("SELECT COUNT(account) FROM MicrosoftAccount account WHERE :current_time <= account.tokenExpire AND account.serverQuitted = 1")
    int countAvailableAccounts(@Param("current_time") long currentTime);

    @Query("SELECT account FROM MicrosoftAccount account WHERE :current_time <= account.tokenExpire AND account.serverQuitted = 1")
    Page<MicrosoftAccount> getAvailableAccounts(@Param("current_time") long currentTime, Pageable pageable);

    /*
      @Query(value = "SELECT * FROM USERS WHERE LASTNAME = ?1",
    countQuery = "SELECT count(*) FROM USERS WHERE LASTNAME = ?1",
    nativeQuery = true)
  Page<User> findByLastname(String lastname, Pageable pageable);
     */
}
