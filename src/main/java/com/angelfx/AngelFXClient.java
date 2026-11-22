package com.angelfx;

import com.mojang.blaze3d.platform.InputConstants;
import java.io.IOException;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import org.lwjgl.glfw.GLFW;

/**
 * AngelFX is client-only. Its cosmetic particles are spawned locally.
 */
public final class AngelFXClient implements ClientModInitializer {
    private static final String MOD_ID = "angelfx";
    private static final int AURA_INTERVAL = 2;
    private static final int TRAIL_INTERVAL = 3;
    private static final double AURA_RADIUS = 0.72;

    private static KeyMapping toggleKey;
    private static KeyMapping modeKey;
    private static KeyMapping menuKey;

    private static boolean enabled = true;
    private static int mode = 0;
    private static int skyMode = 0;
    private static long ticks = 0;

    private static final ParticleOptions[][] PALETTES = new ParticleOptions[][] {
        { ParticleTypes.END_ROD, ParticleTypes.SOUL_FIRE_FLAME, ParticleTypes.WITCH },
        { ParticleTypes.SOUL, ParticleTypes.END_ROD, ParticleTypes.ENCHANT },
        { ParticleTypes.ELECTRIC_SPARK, ParticleTypes.WITCH, ParticleTypes.SOUL_FIRE_FLAME }
    };
    private static final String[] MODE_NAMES = { "Celestial", "Soul", "Arcane" };
    private static final String[] SKY_NAMES = {
        "Vanilla", "Celestial", "Galaxy", "Aurora", "Soul", "Sunset"
    };

      @Override
      public void onInitializeClient() {

      try {
        AngelFXLyrics.initialize(FabricLoader.getInstance().getConfigDir());
        AngelFXLyrics.loadFirstAvailable();
      } catch (IOException e) {
        System.err.println("[AngelFX] Error al cargar las letras:");
        e.printStackTrace();
     }

       KeyMapping.Category category = KeyMapping.Category.register(
         Identifier.fromNamespaceAndPath(MOD_ID, "main")
     );
       

        toggleKey = KeyBindingHelper.registerKeyBinding(new KeyMapping(
            "key.angelfx.toggle", InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_G, category
        ));
        modeKey = KeyBindingHelper.registerKeyBinding(new KeyMapping(
            "key.angelfx.mode", InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_H, category
        ));
        menuKey = KeyBindingHelper.registerKeyBinding(new KeyMapping(
            "key.angelfx.menu", InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_M, category
        ));

        ClientTickEvents.END_CLIENT_TICK.register(AngelFXClient::onClientTick);
    }

    private static void onClientTick(Minecraft client) {
        while (menuKey.consumeClick()) {
            if (client.screen == null) {
                client.setScreen(new AngelFXScreen());
            }
        }

        while (toggleKey.consumeClick()) {
            enabled = !enabled;
            if (client.player != null) {
                client.player.displayClientMessage(Component.literal(
                    "§bAngelFX §7» " + (enabled ? "§aActivado" : "§cDesactivado")
                ), true);
            }
        }

        while (modeKey.consumeClick()) {
            mode = (mode + 1) % MODE_NAMES.length;
            if (client.player != null) {
                client.player.displayClientMessage(Component.literal(
                    "§bAngelFX §7» §fPartículas: §d" + MODE_NAMES[mode]
                ), true);
            }
        }

        if (!enabled || client.level == null || client.player == null || client.player.isSpectator()) {
            return;
        }

        ticks++;
        Player player = client.player;
        if (ticks % AURA_INTERVAL == 0) {
            spawnAura(client, player);
        }
        if (ticks % TRAIL_INTERVAL == 0 && player.onGround()
                && player.getDeltaMovement().horizontalDistanceSqr() > 0.0025) {
            spawnTrail(client, player);
        }
    }

    static boolean isEnabled() {
        return enabled;
    }

    static void setEnabled(boolean value) {
        enabled = value;
    }

    static int getSkyMode() {
        return skyMode;
    }

    static String getSkyName() {
        return SKY_NAMES[skyMode];
    }

    static void setSkyMode(int value) {
        if (value >= 0 && value < SKY_NAMES.length) {
            skyMode = value;
        }
    }

    static String[] getSkyNames() {
        return SKY_NAMES.clone();
    }

    private static void spawnAura(Minecraft client, Player player) {
        double baseY = player.getY() + 0.12;
        double phase = ticks * 0.105;
        ParticleOptions[] palette = PALETTES[mode];

        for (int i = 0; i < 8; i++) {
            double angle = phase + (Math.PI * 2.0 * i / 8.0);
            double x = player.getX() + Math.cos(angle) * AURA_RADIUS;
            double z = player.getZ() + Math.sin(angle) * AURA_RADIUS;
            double y = baseY + 0.48 + Math.sin(phase * 1.7 + i * 0.8) * 0.12;
            ParticleOptions particle = palette[i % palette.length];
            client.level.addParticle(particle, x, y, z,
                -Math.cos(angle) * 0.008, 0.012, -Math.sin(angle) * 0.008);

            double upperAngle = -angle + 0.35;
            double upperX = player.getX() + Math.cos(upperAngle) * 0.48;
            double upperZ = player.getZ() + Math.sin(upperAngle) * 0.48;
            client.level.addParticle(ParticleTypes.END_ROD, upperX, player.getY() + 1.35, upperZ,
                0.0, 0.006, 0.0);
        }
    }

    private static void spawnTrail(Minecraft client, Player player) {
        double backX = -player.getDeltaMovement().x * 0.7;
        double backZ = -player.getDeltaMovement().z * 0.7;
        double x = player.getX() + backX;
        double y = player.getY() + 0.08;
        double z = player.getZ() + backZ;
        ParticleOptions particle = PALETTES[mode][(int) (ticks % PALETTES[mode].length)];
        client.level.addParticle(particle, x, y, z,
            Mth.sin((float) ticks * 0.3F) * 0.008, 0.018,
            Mth.cos((float) ticks * 0.3F) * 0.008);
    }
}
