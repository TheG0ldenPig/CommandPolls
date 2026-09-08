package com.commandpoll;

import com.commandpoll.network.ModNetwork;
import net.minecraftforge.fml.common.Mod;

@Mod(CommandPoll.MODID)
public class CommandPoll {

    public static final String MODID = "commandpoll";

    public CommandPoll() {

        System.out.println("CommandPoll loaded!");

        ModNetwork.register();
    }
}