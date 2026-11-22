package fr.lucien.autosure;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.message.v1.ClientReceiveMessageEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;

import java.util.Optional;

public class AutoSure implements ClientModInitializer {
    private static long armedUntil = 0;

    @Override
    public void onInitializeClient() {
        ClientReceiveMessageEvents.GAME.register((msg, overlay) -> handle(msg));
        ClientReceiveMessageEvents.CHAT.register((msg, sig, sender, params, ts) -> handle(msg));
        ClientPlayConnectionEvents.JOIN.register((handler, sender, client) -> say("mod actif"));
    }

    private static void say(String s) {
        Minecraft mc = Minecraft.getInstance();
        mc.execute(() -> {
            if (mc.player != null) {
                mc.player.displayClientMessage(Component.literal("[AutoSure] " + s), false);
            }
        });
    }

    private static void handle(Component msg) {
        String text = msg.getString().toLowerCase();
        if (text.startsWith("[autosure]")) return;
        long now = System.currentTimeMillis();

        if (text.contains("hideyho")) {
            armedUntil = now + 10000;
            StringBuilder sb = new StringBuilder();
            msg.visit((style, t) -> {
                sb.append("{").append(t).append(" | ").append(style.getClickEvent()).append("} ");
                return Optional.empty();
            }, Style.EMPTY);
            say("message détecté : " + sb);
        }
        if (now > armedUntil) return;

        ClickEvent click = findSure(msg);
        if (click == null) return;
        armedUntil = 0;

        if (click instanceof ClickEvent.RunCommand rc) {
            Minecraft mc = Minecraft.getInstance();
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
