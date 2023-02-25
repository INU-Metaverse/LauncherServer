package kr.goldenmine.inuminecraftlauncher.auth;

import lombok.AllArgsConstructor;
import sk.tomsik68.mclauncher.api.login.ESessionType;
import sk.tomsik68.mclauncher.api.login.ISession;

import java.util.Collections;
import java.util.List;

@AllArgsConstructor
public class MicrosoftSession implements ISession {
    private final String username;
    private final String sessionID;
    private final String uuid;

    @Override
    public String getUsername() {
        return username;
    }

    @Override
    public String getSessionID() {
        return sessionID;
    }

    @Override
    public String getUUID() {
        return uuid;
    }

    @Override
    public ESessionType getType() {
        return ESessionType.MOJANG;
    }

    @Override
    public List<Prop> getProperties() {
        return Collections.emptyList();
    }
}
