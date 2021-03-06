package com.feed_the_beast.ftbquests.util;

import com.feed_the_beast.ftblib.lib.config.ConfigString;
import com.feed_the_beast.ftblib.lib.config.ConfigValue;
import com.feed_the_beast.ftblib.lib.config.ConfigValueInstance;
import com.feed_the_beast.ftblib.lib.gui.IOpenableGui;
import com.feed_the_beast.ftblib.lib.io.Bits;
import com.feed_the_beast.ftblib.lib.io.DataIn;
import com.feed_the_beast.ftblib.lib.io.DataOut;
import com.feed_the_beast.ftblib.lib.util.misc.MouseButton;
import com.feed_the_beast.ftbquests.FTBQuests;
import com.feed_the_beast.ftbquests.gui.GuiSelectQuestObject;
import com.feed_the_beast.ftbquests.quest.QuestObject;
import com.feed_the_beast.ftbquests.quest.QuestObjectType;
import net.minecraft.client.resources.I18n;
import net.minecraft.command.ICommandSender;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.regex.Pattern;

/**
 * @author LatvianModder
 */
public class ConfigQuestObject extends ConfigString
{
	public static final String QO_ID = "ftbquests_object";
	private static final Pattern PATTERN = Pattern.compile("^(\\*|[a-z0-9_:#]{1,32})$");

	private final HashSet<QuestObjectType> types;

	public ConfigQuestObject(String s, Collection<QuestObjectType> t)
	{
		super(s, PATTERN);
		types = new HashSet<>(t);
	}

	public boolean hasValidType(QuestObjectType type)
	{
		return types.contains(type);
	}

	@Override
	public String getName()
	{
		return QO_ID;
	}

	@Override
	public ConfigQuestObject copy()
	{
		return new ConfigQuestObject(getString(), types);
	}

	@Override
	public void onClicked(IOpenableGui gui, ConfigValueInstance inst, MouseButton button)
	{
		if (inst.getCanEdit())
		{
			new GuiSelectQuestObject(this, gui).openGui();
		}
	}

	@Override
	public void writeData(DataOut data)
	{
		data.writeString(getString());

		int i = 0;

		for (QuestObjectType type : types)
		{
			i |= type.getFlag();
		}

		data.writeByte(i);
	}

	@Override
	public void readData(DataIn data)
	{
		setString(data.readString());

		types.clear();

		int i = data.readUnsignedByte();

		for (QuestObjectType type : QuestObjectType.VALUES)
		{
			if (Bits.getFlag(i, type.getFlag()))
			{
				types.add(type);
			}
		}
	}

	@Override
	public boolean setValueFromString(@Nullable ICommandSender sender, String string, boolean simulate)
	{
		if (string.isEmpty() && types.contains(QuestObjectType.FILE))
		{
			if (!simulate)
			{
				setString("");
			}

			return true;
		}

		World world = null;

		if (sender != null)
		{
			world = sender.getEntityWorld();
		}

		QuestObject object = FTBQuests.PROXY.getQuestFile(world).get(string);

		if (object != null && types.contains(object.getObjectType()))
		{
			if (!simulate)
			{
				setString(object.getID());
			}

			return true;
		}

		return false;
	}

	@Override
	public void addInfo(ConfigValueInstance inst, List<String> list)
	{
		if (inst.getCanEdit() && !inst.getDefaultValue().isNull())
		{
			list.add(TextFormatting.AQUA + "Default: " + TextFormatting.RESET + inst.getDefaultValue().getStringForGUI().getFormattedText());
		}

		if (types.size() == 1)
		{
			list.add(TextFormatting.AQUA + "Type: " + TextFormatting.RESET + I18n.format(types.iterator().next().getTranslationKey()));
		}
		else
		{
			list.add(TextFormatting.AQUA + "Types:");

			for (QuestObjectType type : types)
			{
				list.add("> " + I18n.format(type.getTranslationKey()));
			}
		}
	}

	@Override
	public void setValueFromOtherValue(ConfigValue value)
	{
		setString(value.getString());

		if (value instanceof ConfigQuestObject)
		{
			types.clear();
			types.addAll(((ConfigQuestObject) value).types);
		}
	}
}