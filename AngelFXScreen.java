package com.angelfx;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

/**
 * First version of the AngelFX in-game sky-style selector.
 * The selector stores the chosen style; custom sky rendering is a separate
 * rendering implementation and is not claimed to be active in this build.
 */
public final class AngelFXScreen extends Screen {
    private static final int BUTTON_WIDTH = 112;
    private static final int BUTTON_HEIGHT = 22;
    private static final int GAP = 8;

    public AngelFXScreen() {
        super(Component.literal("AngelFX"));
    }

    @Override
    protected void init() {
        String[] names = AngelFXClient.getSkyNames();
        int totalWidth = BUTTON_WIDTH * 2 + GAP;
        int left = (this.width - totalWidth) / 2;
        int top = this.height / 2 - 62;

        for (int i = 0; i < names.length; i++) {
            final int index = i;
            int column = i % 2;
            int row = i / 2;
            int x = left + column * (BUTTON_WIDTH + GAP);
            int y = top + row * (BUTTON_HEIGHT + 7);
            String label = (AngelFXClient.getSkyMode() == i ? "✓ " : "") + names[i];

            this.addRenderableWidget(Button.builder(Component.literal(label), button -> {
                AngelFXClient.setSkyMode(index);
                Minecraft.getInstance().setScreen(new AngelFXScreen());
            }).bounds(x, y, BUTTON_WIDTH, BUTTON_HEIGHT).build());
        }

        int actionY = top + 3 * (BUTTON_HEIGHT + 7) + 3;
        this.addRenderableWidget(Button.builder(
            Component.literal("Efectos: " + (AngelFXClient.isEnabled() ? "ON" : "OFF")),
            button -> {
                AngelFXClient.setEnabled(!AngelFXClient.isEnabled());
                Minecraft.getInstance().setScreen(new AngelFXScreen());
            }
        ).bounds(left, actionY, totalWidth, BUTTON_HEIGHT).build());

        this.addRenderableWidget(Button.builder(Component.literal("Cerrar"), button ->
            this.onClose()
        ).bounds(left, actionY + BUTTON_HEIGHT + 6, totalWidth, BUTTON_HEIGHT).build());
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        graphics.fill(0, 0, this.width, this.height, 0xD9081020);
        graphics.fill(this.width / 2 - 150, 24, this.width / 2 + 150,
            this.height - 24, 0xE8142038);

        graphics.drawCenteredString(this.font, Component.literal("ANGELFX"),
            this.width / 2, 38, 0x8EDBFF);
        graphics.drawCenteredString(this.font, Component.literal("Selector de cielos"),
            this.width / 2, 55, 0xFFFFFF);
        graphics.drawCenteredString(this.font,
            Component.literal("Seleccionado: " + AngelFXClient.getSkyName()),
            this.width / 2, this.height / 2 - 82, 0xC6D8F2);
        graphics.drawCenteredString(this.font,
            Component.literal("El estilo elegido queda guardado durante esta sesión."),
            this.width / 2, this.height - 39, 0xAAB8CC);

        super.render(graphics, mouseX, mouseY, partialTick);
    }

    @Override
    public void onClose() {
        Minecraft.getInstance().setScreen(null);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
