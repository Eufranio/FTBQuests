package com.feed_the_beast.ftbquests.net;

import com.feed_the_beast.ftblib.lib.data.ForgePlayer;
import com.feed_the_beast.ftblib.lib.data.Universe;
import com.feed_the_beast.ftblib.lib.io.DataIn;
import com.feed_the_beast.ftblib.lib.io.DataOut;
import com.feed_the_beast.ftblib.lib.net.MessageToServer;
import com.feed_the_beast.ftblib.lib.net.NetworkWrapper;
import com.feed_the_beast.ftbquests.FTBQuests;
import com.feed_the_beast.ftbquests.quest.QuestObject;
import com.feed_the_beast.ftbquests.quest.ServerQuestFile;
import com.feed_the_beast.ftbquests.util.FTBQuestsTeamData;
import net.minecraft.entity.player.EntityPlayerMP;

/**
 * @author LatvianModder
 */
public class MessageResetProgress extends MessageToServer
{
	private String id;

	public MessageResetProgress()
	{
	}

	public MessageResetProgress(String i)
	{
		id = i;
	}

	@Override
	public NetworkWrapper getWrapper()
	{
		return FTBQuestsNetHandler.GENERAL;
	}

	@Override
	public void writeData(DataOut data)
	{
		data.writeString(id);
	}

	@Override
	public void readData(DataIn data)
	{
		id = data.readString();
	}

	@Override
	public void onMessage(EntityPlayerMP player)
	{
		if (!id.isEmpty() && FTBQuests.canEdit(player))
		{
			QuestObject object = ServerQuestFile.INSTANCE.get(id);

			if (object != null)
			{
				ForgePlayer player1 = Universe.get().getPlayer(player);

				if (player1.team.isValid())
				{
					FTBQuestsTeamData teamData = FTBQuestsTeamData.get(player1.team);
					object.resetProgress(teamData);
					player1.team.markDirty();

					for (ForgePlayer player2 : teamData.team.getMembers())
					{
						if (player2.isOnline())
						{
							new MessageResetProgressResponse(id).sendTo(player2.getPlayer());
						}
					}
				}

				Universe.get().clearCache();
			}
		}
	}
}