package com.feed_the_beast.ftbquests.tile;

import com.feed_the_beast.ftblib.lib.config.ConfigBlockState;
import com.feed_the_beast.ftblib.lib.config.ConfigGroup;
import com.feed_the_beast.ftblib.lib.config.ConfigItemStack;
import com.feed_the_beast.ftblib.lib.config.ConfigNull;
import com.feed_the_beast.ftblib.lib.config.ConfigTeam;
import com.feed_the_beast.ftblib.lib.config.IConfigCallback;
import com.feed_the_beast.ftblib.lib.data.FTBLibAPI;
import com.feed_the_beast.ftblib.lib.tile.EnumSaveType;
import com.feed_the_beast.ftblib.lib.util.BlockUtils;
import com.feed_the_beast.ftbquests.FTBQuests;
import com.feed_the_beast.ftbquests.block.BlockScreen;
import com.feed_the_beast.ftbquests.block.FTBQuestsBlocks;
import com.feed_the_beast.ftbquests.quest.ITeamData;
import com.feed_the_beast.ftbquests.quest.Quest;
import com.feed_the_beast.ftbquests.quest.QuestFile;
import com.feed_the_beast.ftbquests.quest.QuestObjectType;
import com.feed_the_beast.ftbquests.quest.ServerQuestFile;
import com.feed_the_beast.ftbquests.quest.task.QuestTask;
import com.feed_the_beast.ftbquests.quest.task.QuestTaskData;
import com.feed_the_beast.ftbquests.util.ConfigQuestObject;
import net.minecraft.block.BlockHorizontal;
import net.minecraft.block.state.IBlockState;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.items.CapabilityItemHandler;

import javax.annotation.Nullable;
import java.util.Collections;

/**
 * @author LatvianModder
 */
public class TileScreenCore extends TileScreenBase implements IConfigCallback
{
	public EnumFacing facing;
	public String team = "";
	public String quest = "";
	public String task = "";
	public int size = 0;
	public boolean indestructible = false;
	public IBlockState skin = BlockUtils.AIR_STATE;
	public boolean inputOnly = false;
	public ItemStack inputModeIcon = ItemStack.EMPTY;

	private ITeamData cTeam;
	private QuestTask cTask;
	private QuestTaskData cTaskData;

	private Class currentCoreClass, currentPartClass;

	@Override
	protected void writeData(NBTTagCompound nbt, EnumSaveType type)
	{
		if (!type.item)
		{
			nbt.setString("Facing", getFacing().getName());
		}

		if (!team.isEmpty())
		{
			nbt.setString("Team", team);
		}

		cTask = getTask();

		if (cTask != null)
		{
			quest = cTask.quest.getID();
			task = cTask.id;
		}

		if (!quest.isEmpty())
		{
			nbt.setString("Quest", quest);
		}

		if (!task.isEmpty())
		{
			nbt.setString("Task", task);
		}

		if (size > 0)
		{
			nbt.setByte("Size", (byte) size);
		}

		if (indestructible)
		{
			nbt.setBoolean("Indestructible", true);
		}

		if (skin != BlockUtils.AIR_STATE)
		{
			nbt.setString("Skin", BlockUtils.getNameFromState(skin));
		}

		if (inputOnly)
		{
			nbt.setBoolean("InputOnly", true);
		}

		if (!inputModeIcon.isEmpty())
		{
			nbt.setTag("InputModeIcon", inputModeIcon.serializeNBT());
		}
	}

	@Override
	protected void readData(NBTTagCompound nbt, EnumSaveType type)
	{
		if (!type.item)
		{
			facing = EnumFacing.byName(nbt.getString("Facing"));
		}

		team = nbt.getString("Team");
		quest = nbt.getString("Quest");
		task = nbt.getString("Task");
		size = nbt.getByte("Size");
		indestructible = nbt.getBoolean("Indestructible");
		skin = BlockUtils.getStateFromName(nbt.getString("Skin"));
		inputOnly = nbt.getBoolean("InputOnly");
		inputModeIcon = new ItemStack(nbt.getCompoundTag("InputModeIcon"));

		if (inputModeIcon.isEmpty())
		{
			inputModeIcon = ItemStack.EMPTY;
		}

		updateContainingBlockInfo();
	}

	@Override
	public void writeToItem(ItemStack stack)
	{
		NBTTagCompound nbt = new NBTTagCompound();
		writeData(nbt, EnumSaveType.ITEM);

		if (!nbt.isEmpty())
		{
			stack.setTagCompound(nbt);
		}
	}

	@Override
	public void readFromItem(ItemStack stack)
	{
		NBTTagCompound nbt = stack.getTagCompound();
		readData(nbt == null ? new NBTTagCompound() : nbt, EnumSaveType.ITEM);
	}

	@Override
	public boolean hasCapability(Capability<?> capability, @Nullable EnumFacing facing)
	{
		cTask = getTask();

		if (cTask != null && cTask.getMaxProgress() > 0)
		{
			cTaskData = getTaskData();

			if (cTaskData == null)
			{
				return false;
			}

			if (capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY)
			{
				if (cTask.canInsertItem())
				{
					return true;
				}
			}
			else
			{
				if (cTaskData.hasCapability(capability, facing) && cTaskData.task.quest.canStartTasks(cTaskData.teamData))
				{
					return true;
				}
			}
		}

		return super.hasCapability(capability, facing);
	}

	@Override
	@Nullable
	public <T> T getCapability(Capability<T> capability, @Nullable EnumFacing facing)
	{
		cTask = getTask();

		if (cTask != null && cTask.getMaxProgress() > 0)
		{
			cTaskData = getTaskData();

			if (cTaskData == null)
			{
				return null;
			}

			if (capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY)
			{
				if (cTask.canInsertItem())
				{
					return (T) cTaskData;
				}
			}
			else
			{
				T object = cTaskData.getCapability(capability, facing);

				if (object != null && cTaskData.task.quest.canStartTasks(cTaskData.teamData))
				{
					return object;
				}
			}
		}

		return super.getCapability(capability, facing);
	}

	public EnumFacing getFacing()
	{
		if (facing == null)
		{
			IBlockState state = getBlockState();

			if (state.getBlock() == FTBQuestsBlocks.SCREEN)
			{
				facing = getBlockState().getValue(BlockHorizontal.FACING);
			}
			else
			{
				facing = EnumFacing.NORTH;
			}
		}

		return facing;
	}

	@Override
	public TileScreenCore getScreen()
	{
		return this;
	}

	@Override
	public void updateContainingBlockInfo()
	{
		super.updateContainingBlockInfo();
		facing = null;
		cTask = null;
		cTeam = null;
		cTaskData = null;
	}

	@Nullable
	public ITeamData getTeam()
	{
		if (team.isEmpty())
		{
			return null;
		}
		else if (cTeam == null)
		{
			cTeam = FTBQuests.PROXY.getQuestFile(world).getData(team);
		}

		return cTeam;
	}

	@Nullable
	public QuestTask getTask()
	{
		if (quest.isEmpty())
		{
			return null;
		}
		else if (cTask == null || cTask.invalid)
		{
			QuestFile file = FTBQuests.PROXY.getQuestFile(world);
			Quest q = file.getQuest(quest);

			if (q == null || q.tasks.isEmpty())
			{
				cTask = null;
			}
			else if (task.isEmpty())
			{
				cTask = q.tasks.get(0);
			}
			else
			{
				cTask = file.getTask(q.getID() + ':' + task);
			}
		}

		return cTask;
	}

	@Nullable
	public QuestTaskData getTaskData()
	{
		if (quest.isEmpty() || team.isEmpty())
		{
			return null;
		}
		else if (cTaskData == null || cTaskData.task.invalid)
		{
			cTask = getTask();

			if (cTask == null)
			{
				return null;
			}

			cTeam = getTeam();

			if (cTeam == null)
			{
				return null;
			}

			cTaskData = cTeam.getQuestTaskData(cTask);
		}

		return cTaskData;
	}

	@Override
	public AxisAlignedBB getRenderBoundingBox()
	{
		return BlockScreen.getScreenAABB(pos, getFacing(), size);
	}

	@Override
	public double getMaxRenderDistanceSquared()
	{
		double d = 32D * (2 + size);
		return d * d;
	}

	public boolean isOwner(EntityPlayer player)
	{
		return team.isEmpty() || FTBLibAPI.isPlayerInTeam(player.getUniqueID(), team);
	}

	public void onClicked(EntityPlayerMP player, EnumHand hand, double x, double y)
	{
		boolean editor = FTBQuests.canEdit(player);

		if (player.isSneaking())
		{
			if (editor || isOwner(player))
			{
				cTask = getTask();

				if (cTask != null)
				{
					quest = cTask.quest.getID();
					task = cTask.id;
					currentCoreClass = cTask.getScreenCoreClass();
					currentPartClass = cTask.getScreenPartClass();
				}
				else
				{
					currentCoreClass = TileScreenCore.class;
					currentPartClass = TileScreenPart.class;
				}

				boolean editorOrDestructible = editor || !indestructible;
				ConfigGroup group0 = ConfigGroup.newGroup("tile");
				group0.setDisplayName(new TextComponentTranslation("tile.ftbquests.screen.name"));
				ConfigGroup config = group0.getGroup("ftbquests.screen");

				config.add("team", new ConfigTeam(() -> team, v -> team = v), ConfigNull.INSTANCE).setDisplayName(new TextComponentTranslation("ftbquests.team")).setCanEdit(editor);

				config.add("task", new ConfigQuestObject(getTask() == null ? "" : getTask().getID(), Collections.singleton(QuestObjectType.TASK))
				{
					@Override
					public void setString(String v)
					{
						QuestTask t = ServerQuestFile.INSTANCE.getTask(v);

						if (t != null)
						{
							quest = t.quest.getID();
							task = t.id;
						}
					}
				}, ConfigNull.INSTANCE).setCanEdit(editorOrDestructible).setDisplayName(new TextComponentTranslation("ftbquests.task"));

				config.add("skin", new ConfigBlockState(skin)
				{
					@Override
					public void setBlockState(IBlockState v)
					{
						skin = v;
					}
				}, new ConfigBlockState(BlockUtils.AIR_STATE)).setCanEdit(editorOrDestructible);

				if (editor)
				{
					config.addBool("indestructible", () -> indestructible, v -> indestructible = v, false);
				}

				config.addBool("input_only", () -> inputOnly, v -> inputOnly = v, false);
				config.add("input_mode_icon", new ConfigItemStack.SimpleStack(() -> inputModeIcon, v -> inputModeIcon = v), new ConfigItemStack(ItemStack.EMPTY));

				FTBLibAPI.editServerConfig(player, group0, this);
			}

			return;
		}

		if (inputOnly)
		{
			insertItem(player, hand, x, y);
			return;
		}

		cTask = getTask();

		if (cTask == null)
		{
			return;
		}

		if (y >= 0D && y <= 0.17D && !indestructible && cTask.quest.tasks.size() > 1)
		{
			if (!editor && !isOwner(player))
			{
				return;
			}

			currentCoreClass = cTask.getScreenCoreClass();
			currentPartClass = cTask.getScreenPartClass();
			cTask = cTask.quest.tasks.get((cTask.quest.tasks.indexOf(cTask) + 1) % cTask.quest.tasks.size());
			task = cTask.id;

			updateContainingBlockInfo();
			cTask = getTask();

			if (cTask != null && (currentCoreClass != cTask.getScreenCoreClass() || currentPartClass != cTask.getScreenPartClass()))
			{
				updateTiles(cTask);
			}

			markDirty();
			return;
		}

		insertItem(player, hand, x, y);
	}

	private void insertItem(EntityPlayerMP player, EnumHand hand, double x, double y)
	{
		if (!isOwner(player))
		{
			return;
		}

		String top1 = cTask.quest.getDisplayName().getUnformattedText();
		String top2 = cTask.getDisplayName().getUnformattedText();
		double iconY = 0.5D;

		if (!top1.isEmpty() && !top1.equalsIgnoreCase(top2))
		{
			iconY = 0.54D;
		}

		if (y >= iconY - 0.25D && y <= iconY + 0.25D)
		{
			if (!world.isRemote)
			{
				cTaskData = getTaskData();

				if (cTaskData == null)
				{
					return;
				}

				if (cTaskData.task.canInsertItem() && cTaskData.task.getMaxProgress() > 0L && cTaskData.getProgress() < cTaskData.task.getMaxProgress())
				{
					ItemStack stack = player.getHeldItem(hand);

					if (!stack.isEmpty())
					{
						ItemStack stack1 = cTaskData.insertItem(stack, false, false, player);

						if (stack != stack1)
						{
							player.setHeldItem(hand, stack1);
							return;
						}
					}
				}

				//FIXME: MessageSubmitItems.openGUI(cTaskData, player, this);
			}
		}
	}

	public void updateTiles(@Nullable QuestTask task)
	{
		boolean xaxis = getFacing().getAxis() == EnumFacing.Axis.X;

		for (int by = 0; by < size * 2 + 1; by++)
		{
			for (int bx = -size; bx <= size; bx++)
			{
				int offX = xaxis ? 0 : bx;
				int offZ = xaxis ? bx : 0;
				BlockPos pos1 = new BlockPos(pos.getX() + offX, pos.getY() + by, pos.getZ() + offZ);
				world.removeTileEntity(pos1);

				if (bx == 0 && by == 0)
				{
					TileScreenCore core = task == null ? new TileScreenCore() : task.createScreenCore(world);
					core.setWorld(world);
					core.setPos(pos1);
					NBTTagCompound nbt = new NBTTagCompound();
					writeData(nbt, EnumSaveType.SAVE);
					core.readData(nbt, EnumSaveType.SAVE);
					core.validate();
					world.setTileEntity(pos1, core);
				}
				else
				{
					TileScreenPart part = task == null ? new TileScreenPart() : task.createScreenPart(world);
					part.setWorld(world);
					part.setPos(pos1);
					part.setOffset(offX, by, offZ);
					part.validate();
					world.setTileEntity(pos1, part);
				}
			}
		}
	}

	@Override
	public void onConfigSaved(ConfigGroup group, ICommandSender sender)
	{
		updateContainingBlockInfo();
		markDirty();
		BlockUtils.notifyBlockUpdate(world, pos, getBlockState());

		cTask = getTask();

		if (cTask != null && (currentCoreClass != cTask.getScreenCoreClass() || currentPartClass != cTask.getScreenPartClass()))
		{
			updateTiles(cTask);
		}
	}

	@Override
	public void onLoad()
	{
		if (getBlockType() != FTBQuestsBlocks.SCREEN)
		{
			boolean xaxis = getFacing().getAxis() == EnumFacing.Axis.X;

			for (int y = 0; y < size * 2 + 1; y++)
			{
				for (int x = -size; x <= size; x++)
				{
					int offX = xaxis ? 0 : x;
					int offZ = xaxis ? x : 0;
					world.setBlockToAir(new BlockPos(pos.getX() + offX, pos.getY() + y, pos.getZ() + offZ));
				}
			}
		}

		markDirty();
	}

	@Override
	public void markDirty()
	{
		sendDirtyUpdate();
	}
}