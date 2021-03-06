package com.feed_the_beast.ftbquests.gui.chest;

import com.feed_the_beast.ftblib.lib.gui.Button;
import com.feed_the_beast.ftblib.lib.gui.Panel;
import com.feed_the_beast.ftblib.lib.gui.Theme;
import com.feed_the_beast.ftblib.lib.icon.Color4I;
import com.feed_the_beast.ftblib.lib.util.misc.MouseButton;
import com.feed_the_beast.ftbquests.client.ClientQuestFile;
import com.feed_the_beast.ftbquests.net.MessageClaimReward;
import com.feed_the_beast.ftbquests.quest.reward.QuestReward;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.text.TextFormatting;

import java.util.List;

/**
 * @author LatvianModder
 */
public class ButtonReward extends Button
{
	public QuestReward reward = null;

	public ButtonReward(Panel panel)
	{
		super(panel);
	}

	@Override
	public void onClicked(MouseButton button)
	{
		if (reward != null && reward.quest.isComplete(ClientQuestFile.INSTANCE.self) && !ClientQuestFile.INSTANCE.isRewardClaimed(reward))
		{
			new MessageClaimReward(reward.uid).sendToServer();
		}
	}

	@Override
	public void addMouseOverText(List<String> list)
	{
		if (reward == null)
		{
			list.add(TextFormatting.GRAY + I18n.format("tile.ftbquests.chest.output"));
			list.add(TextFormatting.DARK_GRAY + I18n.format("tile.ftbquests.chest.output_desc"));
		}
		else
		{
			reward.addMouseOverText(list);
		}
	}

	@Override
	public void draw(Theme theme, int x, int y, int w, int h)
	{
		if (reward != null)
		{
			reward.getIcon().draw(x, y, w, h);
		}

		if (isMouseOver())
		{
			GlStateManager.pushMatrix();
			GlStateManager.translate(0, 0, 500);
			Color4I.WHITE.withAlpha(150).draw(x, y, w, h);
			GlStateManager.popMatrix();
		}
	}
}