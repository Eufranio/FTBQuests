package com.feed_the_beast.ftbquests.quest.task;

import com.feed_the_beast.ftbquests.quest.ITeamData;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTBase;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.items.IItemHandler;

import javax.annotation.Nullable;

/**
 * @author LatvianModder
 */
public abstract class QuestTaskData<T extends QuestTask> implements ICapabilityProvider, IItemHandler
{
	public final T task;
	public final ITeamData teamData;
	public boolean isComplete = false;

	public QuestTaskData(T q, ITeamData d)
	{
		task = q;
		teamData = d;
	}

	@Nullable
	public abstract NBTBase toNBT();

	public abstract void fromNBT(@Nullable NBTBase nbt);

	public abstract long getProgress();

	public abstract void resetProgress();

	public abstract void completeInstantly();

	public int getRelativeProgress()
	{
		long max = task.getMaxProgress();

		if (max <= 0L)
		{
			return 0;
		}

		long progress = getProgress();

		if (progress <= 0L)
		{
			return 0;
		}
		else if (progress >= max)
		{
			return 100;
		}

		return (int) (progress * 100D / (double) max);
	}

	public String getProgressString()
	{
		return Long.toString(getProgress());
	}

	public String toString()
	{
		return teamData + "@" + task.getID();
	}

	public ItemStack insertItem(ItemStack stack, boolean singleItem, boolean simulate, @Nullable EntityPlayer player)
	{
		return stack;
	}

	@Override
	public final int getSlots()
	{
		return 1;
	}

	@Override
	public final ItemStack getStackInSlot(int slot)
	{
		return ItemStack.EMPTY;
	}

	@Override
	public final ItemStack insertItem(int slot, ItemStack stack, boolean simulate)
	{
		if (task.canInsertItem() && task.getMaxProgress() > 0L && getProgress() < task.getMaxProgress() && !stack.isEmpty())
		{
			return insertItem(stack, false, simulate, null);
		}

		return stack;
	}

	@Override
	public final ItemStack extractItem(int slot, int amount, boolean simulate)
	{
		return ItemStack.EMPTY;
	}

	@Override
	public int getSlotLimit(int slot)
	{
		return 64;
	}

	public final void sync()
	{
		teamData.syncTask(this);
	}

	public boolean submitItems(EntityPlayerMP player)
	{
		if (!task.canInsertItem())
		{
			return false;
		}

		boolean changed = false;

		for (int i = 0; i < player.inventory.mainInventory.size(); i++)
		{
			ItemStack stack = player.inventory.mainInventory.get(i);
			ItemStack stack1 = insertItem(stack, false, false, player);

			if (!ItemStack.areItemStacksEqual(stack, stack1))
			{
				changed = true;
				player.inventory.mainInventory.set(i, stack1);
			}
		}

		return changed;
	}
}