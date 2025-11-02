package com.test.eraser.additional;

import com.mojang.brigadier.Command;
import com.test.eraser.Eraser;
import com.test.eraser.logic.ILivingEntity;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = Eraser.MODID)
public class ModCommands {

    @SubscribeEvent
    public static void onRegisterCommands(RegisterCommandsEvent event) {
        event.getDispatcher().register(
                Commands.literal("eraser")
                        .requires(source -> source.hasPermission(2))
                        .executes(context -> {
                            CommandSourceStack source = context.getSource();
                            if (source.getPlayer() instanceof ILivingEntity player) {
                                player.instantKill();
                            }

                            return Command.SINGLE_SUCCESS;
                        })

        );
    }
}
