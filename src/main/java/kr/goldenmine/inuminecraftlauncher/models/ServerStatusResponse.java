package kr.goldenmine.inuminecraftlauncher.models;

import com.google.gson.annotations.SerializedName;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ServerStatusResponse {

    int availableCounts;

    int totalCounts;
}
