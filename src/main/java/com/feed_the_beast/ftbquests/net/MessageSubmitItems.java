package com.feed_the_beast.ftbquests.net;

import com.feed_the_beast.ftblib.lib.data.Universe;
import com.feed_the_beast.ftblib.lib.io.DataIn;
import com.feed_the_beast.ftblib.lib.io.DataOut;
import com.feed_the_beast.ftblib.lib.net.MessageToServer;
import com.feed_the_beast.ftblib.lib.net.NetworkWrapper;
import com.feed_the_beast.ftbquests.quest.ServerQuestFile;
import com.feed_the_beast.ftbquests.quest.task.QuestTask;
import com.feed_the_beast.ftbquests.util.FTBQuestsTeamData;
import net.minecraft.entity.player.EntityPlayerMP;

/**
 * @author LatvianModder
 */
public class MessageSubmitItems extends MessageToServer
{
	private String task;

	public MessageSubmitItems()
	{
	}

	public MessageSubmitItems(String t)
	{
		task = t;
	}

	@Override
	public NetworkWrapper getWrapper()
	{
		return FTBQuestsNetHandler.GENERAL;
	}

	@Override
	public void writeData(DataOut data)
	{
		data.writeString(task);
	}

	@Override
	public void readData(DataIn data)
	{
		task = data.readString();
	}

	@Override
	public void onMessage(EntityPlayerMP player)
	{
		FTBQuestsTeamData teamData = FTBQuestsTeamData.get(Universe.get().getPlayer(player).team);
		QuestTask t = ServerQuestFile.INSTANCE.getTask(task);

		if (t != null && t.quest.canStartTasks(teamData))
		{
			if (teamData.getQuestTaskData(t).submitItems(player))
			{
				player.inventory.markDirty();
				player.openContainer.detectAndSendChanges();
			}
		}
	}
}