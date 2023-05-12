package net.playeranalytics.plan.gathering.domain;

import com.djrapitops.plan.gathering.domain.PlatformPlayerData;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;

import java.net.*;
import java.util.Optional;
import java.util.UUID;

public class ForgePlayerData implements PlatformPlayerData {

    private final ServerPlayer player;
    private final MinecraftServer server;
    private final String joinAddress;

    public ForgePlayerData(ServerPlayer player, MinecraftServer server, String joinAddress) {
        this.player = player;
        this.server = server;
        this.joinAddress = joinAddress;
    }

    @Override
    public UUID getUUID() {
        return player.getUUID();
    }

    @Override
    public String getName() {
        return player.getName().getString();
    }

    @Override
    public Optional<String> getDisplayName() {
        return Optional.of(player.getDisplayName().getString());
    }

    @Override
    public Optional<Boolean> isOperator() {
        return Optional.of(server.getPlayerList().isOp(player.getGameProfile()));
    }

    @Override
    public Optional<String> getJoinAddress() {
        return Optional.ofNullable(joinAddress);
    }

    @Override
    public Optional<String> getCurrentWorld() {
        return Optional.of(player.getLevel().dimension().location().toString());
    }

    @Override
    public Optional<String> getCurrentGameMode() {
        return Optional.of(player.gameMode.getGameModeForPlayer().name());
    }

    @Override
    public Optional<InetAddress> getIPAddress() {
        return getIPFromSocketAddress();
    }

    private Optional<InetAddress> getIPFromSocketAddress() {
        try {
            SocketAddress socketAddress = player.connection.connection.getRemoteAddress();
            if (socketAddress instanceof InetSocketAddress inetSocketAddress) {
                return Optional.of(inetSocketAddress.getAddress());
            } else if (socketAddress instanceof UnixDomainSocketAddress) {
                // These connections come from the same physical machine
                return Optional.of(InetAddress.getLocalHost());
            }
        } catch (NoSuchMethodError | UnknownHostException e) {
            // Ignored
        }
        return Optional.empty();
    }
}
