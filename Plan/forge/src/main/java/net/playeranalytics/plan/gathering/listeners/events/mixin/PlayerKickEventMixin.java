package net.playeranalytics.plan.gathering.listeners.events.mixin;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;
import net.minecraft.server.commands.KickCommand;
import net.minecraft.server.level.ServerPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import java.util.Collection;

@Mixin(KickCommand.class)
public class PlayerKickEventMixin {

    @Inject(locals = LocalCapture.PRINT, method = "kickPlayers", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/network/ServerGamePacketListenerImpl;disconnect(Lnet/minecraft/network/chat/Component;)V"))
    private static void onKick(CommandSourceStack source, Collection<ServerPlayer> targets, Component reason, CallbackInfoReturnable<Integer> callback) {
        //MinecraftForge.EVENT_BUS.post(new KickEvent(var0));
    }

}
