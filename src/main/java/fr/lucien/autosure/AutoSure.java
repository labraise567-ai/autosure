package fr.lucien.autosure;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.message.v1.ClientReceiveMessageEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.network.protocol.common.ServerboundCustomClickActionPacket;

import java.util.Optional;

public class AutoSure implements ClientModInitializer {
    private static long armedUntil = 0;

    @Override
    public void onInitializeClient() {
        ClientReceiveMessageEvents.GAME.register((msg, overlay) -> handle(msg));
        ClientReceiveMessageEvents.CHAT.register((msg, sig, sender, params, ts) -> handle(msg));
    }

    private static void say(String s) {
        Minecraft mc = Minecraft.getInstance();
        mc.execute(() -> {
            if (mc.player != null) {
                mc.gui.getChat().addClientSystemMessage(Component.literal("[AutoSure] " + s));
            }
        });
    }

    private static void handle(Component msg) {
        String text = msg.getString().toLowerCase();
        if (text.startsWith("[autosure]")) return;
        long now = System.currentTimeMillis();

        if (text.contains("hideyho") && text.contains("how about it")) {
            armedUntil = now + 10000;
        }
        if (now > armedUntil) return;

        ClickEvent click = findSure(msg);
        if (click == null) return;
        armedUntil = 0;

        Minecraft mc = Minecraft.getInstance();
        if (click instanceof ClickEvent.Custom custom) {
            mc.execute(() -> {
                if (mc.player != null) {
                    mc.player.connection.send(
                        new ServerboundCustomClickActionPacket(custom.id(), custom.payload()));
                }
            });
            say("clic envoyé");
        } else if (click instanceof ClickEvent.RunCommand rc) {
            mc.execute(() -> {
                if (mc.player != null) mc.player.connection.sendCommand(rc.command());
            });
            say("clic envoyé");
        } else {
            say("clic non géré : " + click);
        }
    }

    private static ClickEvent findSure(Component msg) {
        ClickEvent[] found = new ClickEvent[1];
        msg.visit((style, t) -> {
            if (found[0] == null && style.getClickEvent() != null
                    && t.toLowerCase().contains("sure")) {
                found[0] = style.getClickEvent();
            }
            return Optional.empty();
        }, Style.EMPTY);
        return found[0];
    }
}
