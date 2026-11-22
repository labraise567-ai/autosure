package fr.lucien.autosure;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.message.v1.ClientReceiveMessageEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;

public class AutoSure implements ClientModInitializer {
    private static final String TRIGGER = "hideyho: how about it?";
    private static long armedUntil = 0; // si "Sure" arrive dans un message séparé

    @Override
    public void onInitializeClient() {
        ClientReceiveMessageEvents.GAME.register((msg, overlay) -> handle(msg));
        ClientReceiveMessageEvents.CHAT.register((msg, sig, sender, params, ts) -> handle(msg));
    }

    private static void handle(Component msg) {
        long now = System.currentTimeMillis();
        if (msg.getString().toLowerCase().contains(TRIGGER)) armedUntil = now + 5000;
        if (now > armedUntil) return;

        ClickEvent click = findSure(msg);
        if (click == null) return;
        armedUntil = 0;

        Minecraft mc = Minecraft.getInstance();
        mc.execute(() -> {
            if (mc.player != null && click instanceof ClickEvent.RunCommand rc) {
                mc.player.connection.sendCommand(rc.command());
            }
        });
    }

    // Parcourt le message et renvoie le ClickEvent du morceau de texte "Sure"
    private static ClickEvent findSure(Component c) {
        if (c.getString().trim().toLowerCase().matches("\\[?sure\\]?")
                && c.getStyle().getClickEvent() != null) {
            return c.getStyle().getClickEvent();
        }
        for (Component sibling : c.getSiblings()) {
            ClickEvent r = findSure(sibling);
            if (r != null) return r;
        }
        return null;
    }
}
