package com.feed_the_beast.ftbquests.integration.buildcraft;

import com.feed_the_beast.ftbquests.quest.task.QuestTaskType;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

/**
 * @author LatvianModder
 */
public class BuildCraftIntegration
{
	public static void preInit()
	{
		MinecraftForge.EVENT_BUS.register(BuildCraftIntegration.class);
	}

	@SubscribeEvent
	public static void registerTasks(RegistryEvent.Register<QuestTaskType> event)
	{
		event.getRegistry().register(new QuestTaskType(MJTask.class, MJTask::new).setRegistryName("buildcraft_mj"));
	}
}