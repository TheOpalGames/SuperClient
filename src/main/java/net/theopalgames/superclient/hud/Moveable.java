package net.theopalgames.superclient.hud;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.IOUtils;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ScaledResolution;
import net.theopalgames.superclient.SuperClientEvent;

public class Moveable 
{
	public boolean show;
	public int locX;
	public int locY; 
	public int offX; 
	public int offY; 
	public String name;
	
	public boolean defaultShow;
	public int defaultLocX;
	public int defaultLocY; 
	public int defaultOffX; 
	public int defaultOffY; 
	
	static ArrayList<Moveable> moveables = new ArrayList<Moveable>();
	
	public Moveable(String name, boolean show, int lx, int ly, int ox, int oy)
	{
		this.name = name;
		this.show = show;
		locX = lx;
		locY = ly;
		offX = ox;
		offY = oy;
		defaultShow = show;
		defaultLocX = lx;
		defaultLocY = ly;
		defaultOffX = ox;
		defaultOffY = oy;
		
		this.load();
		
		moveables.add(this);
	}
	public void setPosition(int lx, int ly, int ox, int oy)
	{
		locX = lx;
		locY = ly;
		offX = ox;
		offY = oy;
	}
	@SuppressWarnings("static-access")
	public void drawBox(int x, int y)
	{
		int color = 0xff00ff00;
		if (!show)
			color = 0xffff0000;
		
		Minecraft.getMinecraft().ingameGUI.drawRect(x-2, y-2, x+2, y+2, color);
		
		if (this == SuperClientEditMenu.hud.selectedMoveable)
		{
			Minecraft.getMinecraft().ingameGUI.drawRect(x-1, y-1, x+1, y+1, 0x7f000000);

			String shown = "Shown";
			if (!show)
				shown = "Hidden";
			if (this == SuperClientEditMenu.hud.hotbarNumbers)
			{
				SuperClientEvent.instance.drawStringAtPosition(0, 0, 0, 30, shown, color);
				SuperClientEvent.instance.drawStringAtPosition(0, 0, 0, 40, "Cannot be moved", 0xffffff);
			}
			else
			{
				String ax = "Left";
				if (locX == 0)
					ax = "Middle";
				if (locX == 1)
					ax = "Right";
				String ay = "Top";
				if (locY == 0)
					ay = "Center";
				if (locY == 1)
					ay = "Bottom";
				SuperClientEvent.instance.drawStringAtPosition(0, 0, 0, 20, shown, color);
				SuperClientEvent.instance.drawStringAtPosition(0, 0, 0, 30, "Alignment: " + ax + " " + ay, 0xffffff);
				SuperClientEvent.instance.drawStringAtPosition(0, 0, 0, 40, "X offset: " + offX, 0xffffff);
				SuperClientEvent.instance.drawStringAtPosition(0, 0, 0, 50, "Y offset: " + offY, 0xffffff);
			}
		}
	}
	public int[] getPos()
	{
		ScaledResolution r = new ScaledResolution(Minecraft.getMinecraft());
		int width = r.getScaledWidth();
		int height = r.getScaledHeight();

		int lX = 0;
		int lY = 0;

		int off = 0;
		if (locY == 0)
			off = 4;
		if (locY == 1)
			off = 8;
		
		if (locX == -1)
			lX = offX;
		else if (locX == 0)
			lX = offX + width / 2;
		else if (locX == 1)
			lX = offX + width;

		if (locY == -1)
			lY = offY;
		else if (locY == 0)
			lY = offY + height / 2 - 5;
		else if (locY == 1)
			lY = offY + height - 9;
		
		lY += off;
		
		return new int[]{lX, lY};
	}
	public void resetToDefault()
	{
		this.show = this.defaultShow;
		this.locX = this.defaultLocX;
		this.locY = this.defaultLocY;
		this.offX = this.defaultOffX;
		this.offY = this.defaultOffY;
	}
	public void save()
	{
		try
		{
			new File("superclient/heads-up-display/" + name + ".yummypie").createNewFile();
			PrintWriter printwriter;
			printwriter = new PrintWriter(new OutputStreamWriter(new FileOutputStream("superclient/heads-up-display/"+ name + ".yummypie"), StandardCharsets.UTF_8));
			printwriter.println(show + "_" + locX + "_" + locY + "_" + offX + "_" + offY);
			printwriter.close();
		} 
		catch (Exception e) 
		{
			e.printStackTrace();
		}
	}
	@SuppressWarnings("deprecation")
	public void load()
	{
		if (!new File("superclient/heads-up-display/"+ name + ".yummypie").exists())
			return;
		try
		{
			List<String> list = IOUtils.readLines(new FileInputStream("superclient/heads-up-display/"+ name + ".yummypie"));
			for (String s : list)
			{
				String[] z = s.split("_");
				if (z[0].contains("true"))
					this.show = true;
				else
					this.show = false;
				this.locX = Integer.parseInt(z[1]);
				this.locY = Integer.parseInt(z[2]);
				this.offX = Integer.parseInt(z[3]);
				this.offY = Integer.parseInt(z[4]);
			}
		} 
		catch (Exception e) 
		{
			e.printStackTrace();
		}

	}
}
		