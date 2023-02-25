package kr.goldenmine.inuminecraftlauncher.login;

import com.google.gson.annotations.SerializedName;
import lombok.*;

import javax.persistence.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Entity
@Table(name = "microsoft_accounts")
public class MicrosoftAccount {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @SerializedName("id")
    private int id;

    @SerializedName("email")
    private String email;

    @SerializedName("password")
    private String password;

    @SerializedName("minecraft_username")
    private String minecraftUsername;

    @SerializedName("minecraft_uuid")
    private String minecraftUuid;

    @SerializedName("token_expire")
    private long tokenExpire;

    @SerializedName("server_borrowed")
    private int serverBorrowed;

    @SerializedName("server_borrowed_expire")
    private long serverBorrowedExpire;

    @SerializedName("server_joined")
    private int serverJoined;

    @SerializedName("server_quitted")
    private int serverQuitted;

    @SerializedName("recent_accessed_ip")
    private String recentAccessedIp;

    @SerializedName("recent_access_token")
    private String recentAccessToken;

    @SerializedName("recent_refresh_token")
    private String recentRefreshToken;

    @SerializedName("recent_profile_token")
    private String recentProfileToken;

    public String toString() {
        return id + ", " +
                email + ", " +
                password + ", " +
                tokenExpire + ", " +
                recentAccessToken + ", " +
                recentRefreshToken + ", " +
                recentProfileToken;
    }
}
