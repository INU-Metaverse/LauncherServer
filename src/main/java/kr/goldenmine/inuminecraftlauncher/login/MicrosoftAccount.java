package kr.goldenmine.inuminecraftlauncher.login;

import com.google.gson.annotations.SerializedName;
import lombok.*;
import net.technicpack.minecraftcore.microsoft.auth.MicrosoftUser;

import javax.persistence.*;

//@Data
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Entity
@Table(name = "microsoft_accounts")
public class MicrosoftAccount {

    public static final long SLEEP_IN_MS = 900 * 1000;

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @SerializedName("id")
    private int id;

    @SerializedName("email")
    private String email;

    @SerializedName("password")
    private String password;

    @SerializedName("token_expire")
    private long tokenExpire;

    @SerializedName("minecraft_username")
    private String minecraftUsername;

    @SerializedName("minecraft_uuid")
    private String minecraftUuid;

    @SerializedName("server_borrowed")
    private int serverBorrowed;

    @SerializedName("server_joined")
    private int serverJoined;

    @SerializedName("server_quitted")
    private int serverQuitted;

    @SerializedName("ip_address")
    private String ipAddress;

    @SerializedName("access_token")
    private String accessToken;

    public boolean checkWhetherRefreshNeeded() {
        final long INNER_TIME = System.currentTimeMillis();

        boolean accessTimeExpired = INNER_TIME + SLEEP_IN_MS * 1.5 >= tokenExpire;
        boolean joinTimeExpired = INNER_TIME + SLEEP_IN_MS * 3.5 >= tokenExpire;
        boolean joined = serverJoined == 1;
        boolean quitted = serverQuitted == 1;
        boolean borrowed = serverBorrowed == 1;

        // accessToken이 null이면 무조건
        boolean t0 = accessToken == null;

        // 토큰 받고, 시간 내 접속 안 한 경우 - O
        boolean t1 = borrowed && (!joined && joinTimeExpired);

        // 토큰 받고, 접속 하고 접속 유지중인 경우 - X
        // 즉 플레이 후 나간 경우
        boolean t2 = quitted;

        // 토큰 받지 않고 리프레시 시간 초과한 경우 - O
        boolean t3 = !borrowed && accessTimeExpired;

        return t0 || t1 || t2 || t3;
    }

    public void updateTokenExpire() {
        setTokenExpire(System.currentTimeMillis() + SLEEP_IN_MS * 4);
    }

    public void initMicrosoftUser(MicrosoftUser user) {
        updateTokenExpire();
        setAccessToken(user.getAccessToken());
        setMinecraftUuid(user.getId());
        setMinecraftUsername(user.getUsername());
        setIpAddress(null);
        setServerBorrowed(0);
        setServerQuitted(0);
        setServerJoined(0);
    }
}
