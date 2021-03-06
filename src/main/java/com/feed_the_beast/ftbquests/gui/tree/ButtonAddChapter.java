package com.feed_the_beast.ftbquests.gui.tree;

import com.feed_the_beast.ftblib.lib.config.ConfigString;
import com.feed_the_beast.ftblib.lib.gui.GuiHelper;
import com.feed_the_beast.ftblib.lib.gui.Panel;
import com.feed_the_beast.ftblib.lib.gui.Theme;
import com.feed_the_beast.ftblib.lib.gui.misc.GuiEditConfigValue;
import com.feed_the_beast.ftblib.lib.util.StringUtils;
import com.feed_the_beast.ftblib.lib.util.misc.MouseButton;
import com.feed_the_beast.ftbquests.gui.QuestsTheme;
import com.feed_the_beast.ftbquests.net.edit.MessageCreateObject;
import com.feed_the_beast.ftbquests.quest.QuestObjectType;
import net.minecraft.client.resources.I18n;
import net.minecraft.nbt.NBTTagCompound;

import java.util.regex.Pattern;

/**
 * @author LatvianModder
 */
public class ButtonAddChapter extends ButtonTab
{
	public ButtonAddChapter(Panel panel)
	{
		super(panel, I18n.format("gui.add"), QuestsTheme.ADD);
	}

	@Override
	public void onClicked(MouseButton button)
	{
		GuiHelper.playClickSound();

		new GuiEditConfigValue("title", new ConfigString("", Pattern.compile("^.+$")), (value, set) ->
		{
			treeGui.openGui();

			if (set)
			{
				NBTTagCompound nbt = new NBTTagCompound();
				nbt.setString("title", value.getString());
				nbt.setString("id", StringUtils.toSnakeCase(value.getString()));
				new MessageCreateObject(QuestObjectType.CHAPTER, "", nbt).sendToServer();
			}
		}).openGui();
	}

	@Override
	public void draw(Theme theme, int x, int y, int w, int h)
	{
		treeGui.borderColor.draw(x, y + h - 1, w + 1, 1);
		treeGui.backgroundColor.draw(x, y + 1, w, h - 2);

		treeGui.borderColor.draw(x + w, y + 1, 1, h - 2);
		icon.draw(x + (w - 16) / 2, y + (h - 16) / 2, 16, 16);

		if (isMouseOver())
		{
			treeGui.backgroundColor.draw(x, y + 1, w, h - 2);
		}
	}
}