package com.feed_the_beast.ftbquests.net.edit;

import com.feed_the_beast.ftblib.lib.io.DataIn;
import com.feed_the_beast.ftblib.lib.io.DataOut;
import com.feed_the_beast.ftblib.lib.net.MessageToServer;
import com.feed_the_beast.ftblib.lib.net.NetworkWrapper;
import com.feed_the_beast.ftbquests.FTBQuests;
import com.feed_the_beast.ftbquests.quest.Quest;
import com.feed_the_beast.ftbquests.quest.QuestObject;
import com.feed_the_beast.ftbquests.quest.ServerQuestFile;
import net.minecraft.entity.player.EntityPlayerMP;

/**
 * @author LatvianModder
 */
public class MessageEditDependency extends MessageToServer
{
	private String quest;
	private String object;
	private boolean add;

	public MessageEditDependency()
	{
	}

	public MessageEditDependency(String q, String o, boolean a)
	{
		quest = q;
		object = o;
		add = a;
	}

	@Override
	public NetworkWrapper getWrapper()
	{
		return FTBQuestsEditNetHandler.EDIT;
	}

	@Override
	public void writeData(DataOut data)
	{
		data.writeString(quest);
		data.writeString(object);
		data.writeBoolean(add);
	}

	@Override
	public void readData(DataIn data)
	{
		quest = data.readString();
		object = data.readString();
		add = data.readBoolean();
	}

	@Override
	public void onMessage(EntityPlayerMP player)
	{
		if (FTBQuests.canEdit(player))
		{
			Quest q = ServerQuestFile.INSTANCE.getQuest(quest);

			if (q != null)
			{
				QuestObject o = ServerQuestFile.INSTANCE.get(object);

				if (o != null && (add ? q.dependencies.add(o.getID()) : q.dependencies.remove(o.getID())))
				{
					q.clearCachedData();
					q.verifyDependencies();
					new MessageEditDependencyResponse(quest, object, add).sendToAll();
					ServerQuestFile.INSTANCE.save();
				}
			}
		}
	}
}