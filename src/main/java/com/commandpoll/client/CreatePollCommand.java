package com.commandpoll.client;

import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.client.Minecraft;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraftforge.client.event.RegisterClientCommandsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(
        modid = "commandpoll",
        value = net.minecraftforge.api.distmarker.Dist.CLIENT
)
public class CreatePollCommand {

    @SubscribeEvent
    public static void register(RegisterClientCommandsEvent event) {
        CommandDispatcher<CommandSourceStack> dispatcher = event.getDispatcher();

        dispatcher.register(
                Commands.literal("createpoll")
                        .executes(context -> {
                            Minecraft.getInstance().setScreen(new CreatePollScreen());
                            return 1;
                        })
        );
    }
}