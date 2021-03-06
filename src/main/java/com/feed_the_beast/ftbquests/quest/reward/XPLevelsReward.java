package com.feed_the_beast.ftbquests.quest.reward;

import com.feed_the_beast.ftblib.lib.config.ConfigGroup;
import com.feed_the_beast.ftblib.lib.icon.Icon;
import com.feed_the_beast.ftbquests.quest.Quest;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.util.text.TextFormatting;

/**
 * @author LatvianModder
 */
public class XPLevelsReward extends QuestReward
{
	private int xpLevels;

	public XPLevelsReward(Quest q, int id, NBTTagCompound nbt)
	{
		super(q, id);
		xpLevels = nbt.getInteger("xp_levels");
	}

	@Override
	public void writeData(NBTTagCompound nbt)
	{
		nbt.setInteger("xp_levels", xpLevels);
	}

	@Override
	public void getConfig(ConfigGroup config)
	{
		config.addInt("xp_levels", () -> xpLevels, v -> xpLevels = v, 1, 1, Integer.MAX_VALUE).setDisplayName(new TextComponentTranslation("ftbquests.reward.ftbquests.xp_levels"));
	}

	@Override
	public void claim(EntityPlayer player)
	{
		player.addExperience(xpLevels);
	}

	@Override
	public Icon getAltIcon()
	{
		return Icon.getIcon("minecraft:items/experience_bottle");
	}

	@Override
	public ITextComponent getAltDisplayName()
	{
		ITextComponent text = new TextComponentString("+" + xpLevels);
		text.getStyle().setColor(TextFormatting.GREEN);
		return new TextComponentTranslation("ftbquests.reward.ftbquests.xp_levels.text", text);
	}
}