package com.feed_the_beast.ftbquests.quest;

import com.feed_the_beast.ftblib.lib.config.ConfigGroup;
import com.feed_the_beast.ftblib.lib.config.ConfigString;
import com.feed_the_beast.ftblib.lib.icon.Icon;
import com.feed_the_beast.ftblib.lib.icon.IconAnimation;
import com.feed_the_beast.ftblib.lib.math.MathUtils;
import com.feed_the_beast.ftblib.lib.util.ListUtils;
import com.feed_the_beast.ftbquests.events.ObjectCompletedEvent;
import com.feed_the_beast.ftbquests.item.ItemMissing;
import com.feed_the_beast.ftbquests.quest.reward.ItemReward;
import com.feed_the_beast.ftbquests.quest.reward.QuestReward;
import com.feed_the_beast.ftbquests.quest.reward.QuestRewardType;
import com.feed_the_beast.ftbquests.quest.task.ItemTask;
import com.feed_the_beast.ftbquests.quest.task.QuestTask;
import com.feed_the_beast.ftbquests.quest.task.QuestTaskType;
import com.feed_the_beast.ftbquests.util.ConfigQuestObject;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTTagString;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraftforge.common.util.Constants;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * @author LatvianModder
 */
public final class Quest extends QuestObject
{
	public static final int POS_LIMIT = 25;
	private static final List<QuestObjectType> DEP_TYPES = Arrays.asList(QuestObjectType.QUEST, QuestObjectType.CHAPTER, QuestObjectType.VARIABLE);

	public final QuestChapter chapter;
	public String description;
	public EnumQuestVisibilityType visibilityType;
	public byte x, y;
	public EnumQuestShape shape;
	public final List<String> text;
	public final Set<String> dependencies;
	public final List<QuestTask> tasks;
	public final List<QuestReward> rewards;
	public int timesCompleted;

	private String cachedID = "";
	private Set<QuestObject> cachedDependencies = null;

	public Quest(QuestChapter c, NBTTagCompound nbt)
	{
		chapter = c;
		readCommonData(nbt);
		description = nbt.getString("description");
		visibilityType = EnumQuestVisibilityType.NAME_MAP.get(nbt.getString("visibility"));
		x = (byte) MathHelper.clamp(nbt.getByte("x"), -POS_LIMIT, POS_LIMIT);
		y = (byte) MathHelper.clamp(nbt.getByte("y"), -POS_LIMIT, POS_LIMIT);
		shape = EnumQuestShape.NAME_MAP.get(nbt.getString("shape"));
		text = new ArrayList<>();

		NBTTagList list = nbt.getTagList("text", Constants.NBT.TAG_STRING);

		for (int k = 0; k < list.tagCount(); k++)
		{
			text.add(list.getStringTagAt(k));
		}

		dependencies = new HashSet<>();
		tasks = new ArrayList<>();
		rewards = new ArrayList<>();

		list = nbt.getTagList("dependencies", Constants.NBT.TAG_STRING);

		if (list.isEmpty())
		{
			NBTBase nbt1 = nbt.getTag("dependency");

			if (nbt1 != null)
			{
				list.appendTag(nbt1);
			}
		}

		for (int i = 0; i < list.tagCount(); i++)
		{
			dependencies.add(list.getStringTagAt(i));
		}

		list = nbt.getTagList("tasks", Constants.NBT.TAG_COMPOUND);

		if (list.isEmpty())
		{
			NBTBase nbt1 = nbt.getTag("task");

			if (nbt1 != null)
			{
				list.appendTag(nbt1);
			}
		}

		for (int i = 0; i < list.tagCount(); i++)
		{
			NBTTagCompound nbt1 = list.getCompoundTagAt(i);
			String type = nbt1.getString("type");

			if (type.equals("quest") || type.equals("variable") || type.equals("chapter") || type.equals("task"))
			{
				dependencies.add(nbt1.getString("object"));
			}
			else
			{
				QuestTask task = QuestTaskType.createTask(this, nbt1);

				if (task != null)
				{
					tasks.add(task);
				}
			}
		}

		list = nbt.getTagList("rewards", Constants.NBT.TAG_COMPOUND);

		if (list.isEmpty())
		{
			NBTBase nbt1 = nbt.getTag("reward");

			if (nbt1 != null)
			{
				list.appendTag(nbt1);
			}
		}

		for (int i = 0; i < list.tagCount(); i++)
		{
			NBTTagCompound nbt1 = list.getCompoundTagAt(i);
			int id = nbt1.getInteger("uid");

			while (id == 0)
			{
				id = MathUtils.RAND.nextInt();
			}

			if (!nbt1.hasKey("type") && !nbt1.hasKey("item"))
			{
				nbt1.removeTag("uid");

				ItemReward reward = new ItemReward(this, id, new NBTTagCompound());
				reward.team = nbt1.getBoolean("team_reward");
				nbt1.removeTag("team_reward");
				reward.stack = ItemMissing.read(nbt1);
				rewards.add(reward);
				continue;
			}

			QuestReward reward = QuestRewardType.createReward(this, id, nbt1);

			if (reward != null)
			{
				rewards.add(reward);
			}
		}

		timesCompleted = nbt.getInteger("times_completed");
	}

	@Override
	public QuestFile getQuestFile()
	{
		return chapter.file;
	}

	@Override
	public QuestObjectType getObjectType()
	{
		return QuestObjectType.QUEST;
	}

	@Override
	public String getID()
	{
		if (cachedID.isEmpty())
		{
			cachedID = chapter.id + ':' + id;
		}

		return cachedID;
	}

	@Override
	public void writeData(NBTTagCompound nbt)
	{
		writeCommonData(nbt);

		if (visibilityType != EnumQuestVisibilityType.NORMAL)
		{
			nbt.setString("type", visibilityType.getName());
		}

		if (x != 0)
		{
			nbt.setByte("x", x);
		}

		if (y != 0)
		{
			nbt.setByte("y", y);
		}

		if (shape != EnumQuestShape.CIRCLE)
		{
			nbt.setString("shape", shape.getName());
		}

		if (!description.isEmpty())
		{
			nbt.setString("description", description);
		}

		if (!text.isEmpty())
		{
			NBTTagList array = new NBTTagList();

			for (String value : text)
			{
				array.appendTag(new NBTTagString(value));
			}

			nbt.setTag("text", array);
		}

		if (!getDependencies().isEmpty())
		{
			NBTTagList array = new NBTTagList();

			for (QuestObject object : getDependencies())
			{
				if (!object.invalid)
				{
					array.appendTag(new NBTTagString(object.getID()));
				}
			}

			if (array.tagCount() == 1)
			{
				nbt.setTag("dependency", array.get(0));
			}
			else if (!array.isEmpty())
			{
				nbt.setTag("dependencies", array);
			}
		}

		if (!tasks.isEmpty())
		{
			NBTTagList array = new NBTTagList();

			for (QuestTask task : tasks)
			{
				QuestTaskType type = QuestTaskType.getType(task.getClass());

				if (type != null)
				{
					NBTTagCompound nbt1 = new NBTTagCompound();
					task.writeData(nbt1);
					nbt1.setString("id", task.id);

					if (task.getClass() != ItemTask.class)
					{
						nbt1.setString("type", type.getTypeForNBT());
					}

					task.writeCommonData(nbt1);
					array.appendTag(nbt1);
				}
			}

			if (array.tagCount() == 1)
			{
				nbt.setTag("task", array.get(0));
			}
			else if (!array.isEmpty())
			{
				nbt.setTag("tasks", array);
			}
		}

		if (!rewards.isEmpty())
		{
			NBTTagList array = new NBTTagList();

			for (QuestReward reward : rewards)
			{
				QuestRewardType type = QuestRewardType.getType(reward.getClass());

				if (type != null)
				{
					NBTTagCompound nbt1 = new NBTTagCompound();
					reward.writeData(nbt1);
					nbt1.setInteger("id", reward.uid);

					if (reward.getClass() != ItemReward.class)
					{
						nbt1.setString("type", type.getTypeForNBT());
					}

					reward.writeCommonData(nbt1);
					array.appendTag(nbt1);
				}
			}

			if (array.tagCount() == 1)
			{
				nbt.setTag("reward", array.get(0));
			}
			else if (!array.isEmpty())
			{
				nbt.setTag("rewards", array);
			}
		}

		if (timesCompleted > 0)
		{
			nbt.setInteger("times_completed", timesCompleted);
		}
	}

	@Override
	public long getProgress(ITeamData data)
	{
		long progress = 0L;

		for (QuestTask task : tasks)
		{
			if (!task.invalid)
			{
				progress += task.getProgress(data);
			}
		}

		return progress;
	}

	@Override
	public long getMaxProgress()
	{
		long maxProgress = 0L;

		for (QuestTask task : tasks)
		{
			if (!task.invalid)
			{
				maxProgress += task.getMaxProgress();
			}
		}

		return maxProgress;
	}

	@Override
	public int getRelativeProgress(ITeamData data)
	{
		int progress = 0;

		int s = 0;

		for (QuestTask task : tasks)
		{
			if (!task.invalid)
			{
				progress += task.getRelativeProgress(data);
				s++;
			}
		}

		return fixRelativeProgress(progress, s);
	}

	@Override
	public boolean isComplete(ITeamData data)
	{
		for (QuestTask task : tasks)
		{
			if (!task.invalid && !task.isComplete(data))
			{
				return false;
			}
		}

		for (QuestObject object : getDependencies())
		{
			if (!object.isComplete(data))
			{
				return false;
			}
		}

		return true;
	}

	@Override
	public void onCompleted(ITeamData data)
	{
		timesCompleted++;
		super.onCompleted(data);
		new ObjectCompletedEvent.QuestEvent(data, this).post();

		if (chapter.isComplete(data))
		{
			chapter.onCompleted(data);
		}
	}

	@Override
	public void resetProgress(ITeamData data)
	{
		for (QuestTask task : tasks)
		{
			task.resetProgress(data);
		}

		data.unclaimRewards(rewards);
	}

	@Override
	public void completeInstantly(ITeamData data)
	{
		for (QuestTask task : tasks)
		{
			task.completeInstantly(data);
		}
	}

	public boolean canStartTasks(ITeamData data)
	{
		for (QuestObject object : getDependencies())
		{
			if (!object.isComplete(data))
			{
				return false;
			}
		}

		return getVisibility(data).isVisible();
	}

	@Override
	public Icon getAltIcon()
	{
		List<Icon> list = new ArrayList<>();

		for (QuestTask task : tasks)
		{
			list.add(task.getIcon());
		}

		return IconAnimation.fromList(list, false);
	}

	@Override
	public ITextComponent getAltDisplayName()
	{
		for (QuestTask task : tasks)
		{
			return task.getDisplayName();
		}

		return new TextComponentTranslation("ftbquests.unnamed");
	}

	@Override
	public void deleteSelf()
	{
		super.deleteSelf();
		chapter.quests.remove(this);
	}

	@Override
	public void deleteChildren()
	{
		for (QuestTask task : tasks)
		{
			task.deleteChildren();
			task.invalid = true;
		}

		tasks.clear();
	}

	@Override
	public void onCreated()
	{
		chapter.quests.add(this);

		if (!tasks.isEmpty())
		{
			for (QuestTask task : ListUtils.clearAndCopy(tasks))
			{
				task.onCreated();
			}
		}
	}

	@Override
	public void getConfig(ConfigGroup config)
	{
		config.addInt("x", () -> x, v -> x = (byte) v, 0, -POS_LIMIT, POS_LIMIT);
		config.addInt("y", () -> y, v -> y = (byte) v, 0, -POS_LIMIT, POS_LIMIT);
		config.addEnum("shape", () -> shape, v -> shape = v, EnumQuestShape.NAME_MAP);
		config.addEnum("visibility", () -> visibilityType, v -> visibilityType = v, EnumQuestVisibilityType.NAME_MAP);
		config.addString("description", () -> description, v -> description = v, "");
		config.addList("text", text, new ConfigString(""), ConfigString::new, ConfigString::getString);
		config.addList("dependencies", dependencies, new ConfigQuestObject("", DEP_TYPES), v -> new ConfigQuestObject(v, DEP_TYPES), ConfigQuestObject::getString).setDisplayName(new TextComponentTranslation("ftbquests.dependencies"));
	}

	public EnumVisibility getVisibility(@Nullable ITeamData data)
	{
		EnumVisibility v = EnumVisibility.VISIBLE;

		for (QuestObject object : getDependencies())
		{
			if (object instanceof Quest)
			{
				v = v.weakest(((Quest) object).getVisibility(data));

				if (v.isInvisible())
				{
					return EnumVisibility.INVISIBLE;
				}
			}
		}

		if (data == null && visibilityType != EnumQuestVisibilityType.NORMAL)
		{
			return visibilityType == EnumQuestVisibilityType.SECRET_ONE || visibilityType == EnumQuestVisibilityType.SECRET_ALL ? EnumVisibility.SECRET : EnumVisibility.INVISIBLE;
		}

		/*
		switch (getVisibilityType())
		{
			case SECRET_ONE:
				for (String value : dependencies)
				{
					QuestObject object = chapter.file.get(value);

					if (object != null && object.isComplete(data))
					{
						return true;
					}
				}

				return false;
			case INVISIBLE:
				for (String value : dependencies)
				{
					QuestObject object = chapter.file.get(value);

					if (object != null && !object.isComplete(data))
					{
						return false;
					}
				}

				return true;
			default:
				return true;
		}
		*/

		return v;
	}

	public QuestTask getTask(int index)
	{
		if (tasks.isEmpty())
		{
			throw new IllegalStateException("Quest has no tasks!");
		}
		else if (index <= 0)
		{
			return tasks.get(0);
		}
		else if (index >= tasks.size())
		{
			return tasks.get(tasks.size() - 1);
		}

		return tasks.get(index);
	}

	@Override
	public void clearCachedData()
	{
		super.clearCachedData();
		cachedID = "";
		cachedDependencies = null;

		for (QuestTask task : tasks)
		{
			task.clearCachedData();
		}
	}

	public boolean hasDependency(QuestObject object)
	{
		return getDependencies().contains(object);
	}

	public Set<QuestObject> getDependencies()
	{
		if (cachedDependencies == null)
		{
			cachedDependencies = new HashSet<>();

			for (String id : dependencies)
			{
				QuestObject object = chapter.file.get(id);

				if (object != null)
				{
					cachedDependencies.add(object);
				}
			}

			if (cachedDependencies.isEmpty())
			{
				cachedDependencies = Collections.emptySet();
			}
		}

		return cachedDependencies;
	}

	public void verifyDependencies()
	{
		//FTBQuests.LOGGER.error("Removed looping dependency '" + task.getID() + "' with erroring ID '" + task.objectId + "'");
	}
}