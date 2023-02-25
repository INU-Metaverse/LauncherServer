package kr.goldenmine.inuminecraftlauncher.models;

import com.google.gson.annotations.SerializedName;


public class AccountResponse {
    @SerializedName("profile_token")
    String profileToken;

    @SerializedName("username")
    String userName;

}

