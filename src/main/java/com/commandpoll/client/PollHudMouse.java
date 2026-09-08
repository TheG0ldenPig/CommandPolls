package com.commandpoll.client;

import net.minecraft.client.Minecraft;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(
        modid = "commandpoll",
        value = Dist.CLIENT
)
public class PollHudMouse {

    @SubscribeEvent
    public static void onMouseButton(
            InputEvent.MouseButton.Pre event
    ) {
        if (event.getButton() != 0) {
            return;
        }

        if (event.getAction() != 1) {
            return;
        }

        Minecraft minecraft =
                Minecraft.getInstance();

        if (minecraft.player == null) {
            return;
        }

        double mouseX =
                minecraft.mouseHandler.xpos()
                        * minecraft.getWindow().getGuiScaledWidth()
                        / minecraft.getWindow().getScreenWidth();

        double mouseY =
                minecraft.mouseHandler.ypos()
                        * minecraft.getWindow().getGuiScaledHeight()
                        / minecraft.getWindow().getScreenHeight();

        PollHud.handleClick(
                mouseX,
                mouseY
        );
    }
}