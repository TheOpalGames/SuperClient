package net.theopalgames.superclient;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.io.IOUtils;
import org.apache.logging.log4j.core.util.Throwables;
import org.lwjgl.input.Keyboard;

import com.google.common.base.Charsets;
import com.google.common.io.Files;

import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.InputEvent.KeyInputEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.ClientTickEvent;
import net.minecraftforge.fml.common.network.FMLNetworkEvent.ClientDisconnectionFromServerEvent;
import net.theopalgames.superclient.hud.SuperClientEditMenu;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.AbstractClientPlayer;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.translation.I18n;
import net.minecraftforge.client.event.ClientChatReceivedEvent;
import net.minecraftforge.client.event.FOVUpdateEvent;
import net.minecraftforge.client.event.MouseEvent;
import net.minecraftforge.client.event.RenderGameOverlayEvent;


@SuppressWarnings("deprecation")
public class SuperClientEvent
{

	Minecraft mc = Minecraft.getMinecraft();
	ScaledResolution screen = new ScaledResolution(mc);
	public int width = screen.getScaledWidth();
	public int height = screen.getScaledHeight();

	static PrintWriter printwriter;

	public static SuperClientEvent instance;
	
	static boolean[] activecamslot = new boolean[]{false, false, false, false, false, false, false, false, false};

	/** Saved coordinates in current profile, pre-loaded to prevent having to read the coordinate files too much*/
	static int[][] coordinates = new int[9][3];
	/** autos and autos + 1 are the key bind indexes for autosprint and autosneak*/
	//static final int autos = 21;
	/** When true, the player's camera yaw will be increased by 180 degrees*/
	static boolean lookbehind = false;
	/** Which zoom keys are pressed, stores value from KeyPressed event to FOVUpdateEvent*/
	static boolean[] zooms = new boolean[]{false, false, false, false, false, false};
	/** This is the counter for the gliding animation progress when a boss bar appears or disappears. Modifies the position of held items*/
	static int move = 0;
	/** Whether or not there is a bossbar visible*/
	static boolean boss = false;
	/** Whether or not keybinds are registered (prevents double registry of keybinds), because the bindKeys() method is called twice*/
	public static boolean registered = false;
	/** The mod's keybinds*/
	public static KeyBinding[] keyBindings;
	/** Is Autosprint active?*/
	static boolean sprint = false;
	/** Is Autosneak active?*/
	static boolean sneak = false;
	/** Color for Forward key on move pad. It changes when pressed to light up*/
	static int col1 = 0;
	/** Color for Backward key on move pad. It changes when pressed to light up*/
	static int col2 = 0;
	/** Color for Left key on move pad. It changes when pressed to light up*/
	static int col3 = 0;
	/** Color for Right key on move pad. It changes when pressed to light up*/
	static int col4 = 0;
	/** Color for Jump key on move pad. It changes when pressed to light up*/
	static int col5 = 0;
	/** Brightness timer - set to 60 when a brightness keybind is pressed. Constantly decreases if greater than 0. Brightness text is shown when this value is greater than 0.*/
	static int btimer = 0;
	/** Extra offset to the held items, added to 'move'. Used when a custom string is displayed*/
	static int extramove = 0;
	/** Yaw modifier to camera*/
	static double yaw = 0;
	/** Pitch modifier to camera*/
	static double pitch = 0;
	/** Roll modifier to camera*/
	static double roll = 0;

	static boolean showServerMenu = false;

	static boolean increasecamdist = false;
	static boolean decreasecamdist = false;

	static boolean showFriends = false;
	static String instCurrent = "";

	static String menuTitle = "";
	static String menuSubtitle = "";
	static ArrayList<String[]> menuCommands = new ArrayList<String[]>();

	static ArrayList<AbstractClientPlayer> onlineFriends = new ArrayList<AbstractClientPlayer>();

	static ArrayList<Long> leftClicks = new ArrayList<Long>();
	static ArrayList<Long> rightClicks = new ArrayList<Long>();
	static long lastLeftClick = 0;
	static long prevLastLeftClick = 0;
	static long lastRightClick = 0;
	static long prevLastRightClick = 0;

	static String attackText = "";
	static String defenseText = "";
	static String timerText = "";

	static long attackTime = 0;
	static long defenseTime = 0;


	
	enum KnownServer{none, theopalgames};
	static KnownServer currentServer = KnownServer.none;

	@SuppressWarnings("static-access")
	public SuperClientEvent()
	{
		this.instance = this;

		if (!SuperClient.instructions.isEmpty())
			instCurrent = SuperClient.instructions.remove(0);
		
		bindKeys();
	}
	/** Allocate and bind keys. NOTE - There is no language localization yet*/
	public void bindKeys()
	{
		if (!registered)
		{
			keyBindings = new KeyBinding[25];

			keyBindings[0] = new KeyBinding("Zoom 1 - 50", Keyboard.KEY_V, "Zoom");
			keyBindings[1] = new KeyBinding("Zoom 2 - 60", Keyboard.KEY_B, "Zoom");
			keyBindings[2] = new KeyBinding("Zoom 3 - 70", Keyboard.KEY_N, "Zoom");
			keyBindings[3] = new KeyBinding("Zoom 4 - 80", Keyboard.KEY_M, "Zoom");
			keyBindings[4] = new KeyBinding("Zoom 5 - 90", Keyboard.KEY_COMMA, "Zoom");
			keyBindings[5] = new KeyBinding("Zoom 6 - 100", Keyboard.KEY_PERIOD, "Zoom");
			keyBindings[6] = new KeyBinding("Increase Brightness", Keyboard.KEY_R, "Brightness");
			keyBindings[7] = new KeyBinding("Decrease Brightness", Keyboard.KEY_Z, "Brightness");
			keyBindings[8] = new KeyBinding("Full Brightness", Keyboard.KEY_G, "Brightness");
			keyBindings[9] = new KeyBinding("High Brightness", Keyboard.KEY_H, "Brightness");
			keyBindings[10] = new KeyBinding("Bright Brightness", Keyboard.KEY_J, "Brightness");
			keyBindings[11] = new KeyBinding("Moody Brightness", Keyboard.KEY_K, "Brightness");
			//keyBindings[12] = new KeyBinding("Show/Hide Item Info", Keyboard.KEY_F6, "Display");
			//keyBindings[13] = new KeyBinding("Show/Hide Extra Details", Keyboard.KEY_F12, "Display");
			//keyBindings[14] = new KeyBinding("Show/Hide Hotbar Numbers", Keyboard.KEY_F9, "Display");
			//keyBindings[15] = new KeyBinding("Show/Hide Status Effects", Keyboard.KEY_F7, "Display");
			//keyBindings[12] = new KeyBinding("Show/Hide Move Controls", Keyboard.KEY_F8, "Display");
			keyBindings[12] = new KeyBinding("Save Position", Keyboard.KEY_EQUALS, "Coordinates");
			keyBindings[13] = new KeyBinding("Delete Position", Keyboard.KEY_MINUS, "Coordinates");
			//keyBindings[15] = new KeyBinding("Show/Hide Coordinates", Keyboard.KEY_F10, "Display");
			keyBindings[14] = new KeyBinding("Closer to Player", Keyboard.KEY_O, "Camera");
			keyBindings[15] = new KeyBinding("Farther from Player", Keyboard.KEY_I, "Camera");
			keyBindings[16] = new KeyBinding("Change View Angle", Keyboard.KEY_F10, "Camera");
			keyBindings[17] = new KeyBinding("Lock View Angle", Keyboard.KEY_P, "Camera");
			keyBindings[18] = new KeyBinding("Look Behind", Keyboard.KEY_LMENU, "Camera");
			keyBindings[19] = new KeyBinding("Miscellaneous Menu", Keyboard.KEY_Y, "Super Client");
			keyBindings[20] = new KeyBinding("Online Friends", Keyboard.KEY_SEMICOLON, "Friends");
			keyBindings[21] = new KeyBinding("Autosprint", Keyboard.KEY_GRAVE, "Automatic Actions");
			keyBindings[22] = new KeyBinding("Autosneak", Keyboard.KEY_U, "Automatic Actions");
			keyBindings[23] = new KeyBinding("Edit Display Menu", Keyboard.KEY_F12, "Super Client");
			keyBindings[24] = new KeyBinding("Teleport", Keyboard.KEY_0, "Coordinates");

			for (int i = 0; i < keyBindings.length; ++i)
			{
				ClientRegistry.registerKeyBinding(keyBindings[i]);
			}
			registered = true;
		}
	}
	/** Do stuff when keys are pressed*/
	@SuppressWarnings("static-access")
	@SubscribeEvent
	public void key(KeyInputEvent event)
	{
		Minecraft.getMinecraft().gameSettings.keyBindAttack.updateKeyBindState();
		//0-6 - zooms
		if (keyBindings[0].isKeyDown())
			zooms[0] = true;
		else
			zooms[0] = false;
		if (keyBindings[1].isKeyDown())
			zooms[1] = true;
		else
			zooms[1] = false;
		if (keyBindings[2].isKeyDown())
			zooms[2] = true;
		else
			zooms[2] = false;
		if (keyBindings[3].isKeyDown())
			zooms[3] = true;
		else
			zooms[3] = false;
		if (keyBindings[4].isKeyDown())
			zooms[4] = true;
		else
			zooms[4] = false;
		if (keyBindings[5].isKeyDown())
			zooms[5] = true;
		else
			zooms[5] = false;

		//6-7 - Brightness increase/decrease
		if (keyBindings[6].isKeyDown())
		{
			btimer = 60;
			if (Minecraft.getMinecraft().gameSettings.gammaSetting == 15)
			{}
			else
				Minecraft.getMinecraft().gameSettings.gammaSetting++;
		}
		if (keyBindings[7].isKeyDown())
		{
			btimer = 60;
			if (Minecraft.getMinecraft().gameSettings.gammaSetting == -15)
			{}
			else
				Minecraft.getMinecraft().gameSettings.gammaSetting--;
		}
		//8-11 - preset brightnesses
		if (keyBindings[8].isKeyDown())
		{
			btimer = 60;
			Minecraft.getMinecraft().gameSettings.gammaSetting = 15;
		}
		if (keyBindings[9].isKeyDown())
		{
			btimer = 60;
			Minecraft.getMinecraft().gameSettings.gammaSetting = 6;
		}
		if (keyBindings[10].isKeyDown())
		{
			btimer = 60;
			Minecraft.getMinecraft().gameSettings.gammaSetting = 1;
		}
		if (keyBindings[11].isKeyDown())
		{
			btimer = 60;
			Minecraft.getMinecraft().gameSettings.gammaSetting = 0;
		}
		//12-16 and 19 - Show/Hide parts of the HUD - REMOVED
		/*if (keyBindings[12].isPressed())
	    {
	    	showGui = !showGui;
	    }
	    if (keyBindings[15].isPressed())
	    {
	    	showEffects = !showEffects;
	    }
	    if (keyBindings[13].isPressed())
	    {
	    	showInfo = !showInfo;
	    }
	    if (keyBindings[16].isPressed())
	    {
	    	showMove = !showMove;
	    }
	    if (keyBindings[14].isPressed())
	    {
	    	showHotbar = !showHotbar;
	    }*/
		//17-18 - add/remove coordinate saves
		if (keyBindings[12].isKeyDown())
		{
			//Check to see if a hotbar slot is held with the save coordinate slot, then, if one is, save coordinate to that slot
			int activeslot = -1;
			for (int i = 0; i < 9; i++)
				if (Minecraft.getMinecraft().gameSettings.keyBindsHotbar[i].isPressed())
					activeslot=i;

			if (activeslot!=-1)
			{
				int x = (int)Minecraft.getMinecraft().player.posX;
				int y = (int)Minecraft.getMinecraft().player.posY;
				int z = (int)Minecraft.getMinecraft().player.posZ;
				if (Minecraft.getMinecraft().player.posX < 0)
					x -= 1;
				if (Minecraft.getMinecraft().player.posY < 0)
					y -= 1;
				if (Minecraft.getMinecraft().player.posZ < 0)
					z -= 1;
				addCoords(x, y, z, activeslot);
				loadCoords();
			}
		}
		if (keyBindings[13].isKeyDown())
		{
			//Check to see if a hotbar slot is held with the save coordinate slot, then, if one is, remove saved coordinate to that slot
			int activeslot = -1;
			for (int i = 0; i < 9; i++)
				if (Minecraft.getMinecraft().gameSettings.keyBindsHotbar[i].isPressed())
					activeslot=i;

			if (activeslot!=-1)
			{
				addCoords(0, Integer.MIN_VALUE, 0, activeslot);
				loadCoords();
			}
		}
		//teleport to coordinate
		if (keyBindings[24].isKeyDown())
		{
			//Check to see if a hotbar slot is held with the save coordinate slot, then, if one is, remove saved coordinate to that slot
			int activeslot = -1;
			for (int i = 0; i < 9; i++)
				if (Minecraft.getMinecraft().gameSettings.keyBindsHotbar[i].isPressed())
					activeslot=i;

			if (activeslot!=-1)
			{
				int[] coords = getCoords(activeslot);
				if (coords[1] != Integer.MIN_VALUE)
					Minecraft.getMinecraft().player.sendChatMessage("/tp @s " + coords[0] + " " + coords[1] + " " + coords[2]);
			}
		}
		/*if (keyBindings[14].isPressed())
	    {
	    	showCoordinates = !showCoordinates;
	    }*/
		if (SuperClientConfig.cam.camera)
		{
			//camera controls
			//20-21 - Change 3rd person view dist
			if (keyBindings[14].isKeyDown())
			{
				decreasecamdist = true;
			}
			else
			{
				decreasecamdist = false;
			}
			if (keyBindings[15].isKeyDown())
			{
				increasecamdist = true;
			}
			else
			{
				increasecamdist = false;
			}

			//22 - change view
			if (keyBindings[16].isKeyDown())
			{
				//checks to see if any hotbar keys are held, then changes the view accordingly
				for (int i = 0; i < 9; i++)
					if (Minecraft.getMinecraft().gameSettings.keyBindsHotbar[i].isKeyDown())
					{
						activecamslot[i] = true;
					}
					else
					{
						activecamslot[i] = false;
					}

			}
			if (keyBindings[17].isPressed())
			{
				//toggle angle lock
				SuperClientEntityRenderer.camLocked = !SuperClientEntityRenderer.camLocked;
			}
			//if you aren't looking behind, and the lookbehind key is held, look behind.
			if (keyBindings[18].isKeyDown())
			{
				if (!lookbehind)
					yaw += 180;
				lookbehind = true;
			}
			else //if you are looking behind, and the lookbehind key is released, stop looking behind.
			{
				if (lookbehind)
					yaw -= 180;
				lookbehind = false;
			}
		}
		if (keyBindings[19].isPressed())
		{
			if (!SuperClient.instructions.isEmpty())
				instCurrent = SuperClient.instructions.remove(0);
			else
			{
				if (menuTitle != "")
					showServerMenu = !showServerMenu;
				else
					showServerMenu = false;

			}
		}
		if (keyBindings[20].isKeyDown())
		{
			showFriends = !showFriends;
		}
		if (SuperClient.toggle) //autosprint and autosneak. toggle is an old name XD
		{
			if (keyBindings[21].isPressed())
			{
				if (sprint) //stop sprinting key from being 'pressed' - otherwise minecraft will think you are clicking sprint even after autosprint is off
					KeyBinding.setKeyBindState(Minecraft.getMinecraft().gameSettings.keyBindSprint.getKeyCode(), false);

				//stop autosneak. similar reason above
				if (sneak && !Minecraft.getMinecraft().gameSettings.keyBindSneak.isPressed())
					KeyBinding.setKeyBindState(Minecraft.getMinecraft().gameSettings.keyBindSneak.getKeyCode(), false);

				//actually disable autosneak
				if (sneak)
					sneak = false;

				sprint = !sprint;
			}
			if (keyBindings[22].isPressed())
			{
				//if (sneak) //stop sneak behavior after autosneak is turned off. otherwise, you would keep sneaking until you pressed shift
				//	KeyBinding.setKeyBindState(Minecraft.getMinecraft().gameSettings.keyBindSneak.getKeyCode(), false);

				if (sprint && !Minecraft.getMinecraft().gameSettings.keyBindSprint.isPressed()) //turn autosprint off
					KeyBinding.setKeyBindState(Minecraft.getMinecraft().gameSettings.keyBindSprint.getKeyCode(), false);

				if (sprint) //actually disable autosprint
					sprint = false;

				sneak = !sneak;
			}
		}
		if (keyBindings[23].isPressed())
		{
			Minecraft.getMinecraft().displayGuiScreen(new SuperClientEditMenu());
		}
		if (showServerMenu)
		{
			for (int i = 0; i < 9; i++)
			{
				if (Minecraft.getMinecraft().gameSettings.keyBindsHotbar[i].isPressed())
				{
					if (menuCommands.size() > i)
					{
						Minecraft.getMinecraft().player.sendChatMessage(menuCommands.get(i)[1]);
						showServerMenu = false;
					}
				}
			}
		}

		if (sneak)
		{
			KeyBinding.setKeyBindState(Minecraft.getMinecraft().gameSettings.keyBindSneak.getKeyCode(), true);
		}
	}

	@SuppressWarnings("static-access")
	public void leftClick()
	{
		if (SuperClientConfig.display.clickrate.clickrateoptions.intervalLeft)
		{
			this.prevLastLeftClick = this.lastLeftClick;
			this.lastLeftClick = System.currentTimeMillis();
		}
		else
		{
			this.leftClicks.add(System.currentTimeMillis());
		}
	}
	@SubscribeEvent
	public void click(MouseEvent e)
	{
		if (!e.isButtonstate())
			return;
		if (e.getButton() == 0)
		{
			if (Minecraft.getMinecraft().gameSettings.keyBindAttack.getDisplayName().equals("Left Click"))
				leftClick();
			if (Minecraft.getMinecraft().gameSettings.keyBindUseItem.getDisplayName().equals("Left Click"))
				rightClick();
		}
		else if (e.getButton() == 1)
		{
			if (Minecraft.getMinecraft().gameSettings.keyBindAttack.getDisplayName().equals("Right Click"))
				leftClick();
			if (Minecraft.getMinecraft().gameSettings.keyBindUseItem.getDisplayName().equals("Right Click"))
				rightClick();
		}
		else if (e.getButton() == 2)
		{
			if (Minecraft.getMinecraft().gameSettings.keyBindAttack.getDisplayName().equals("Middle Click"))
				leftClick();
			if (Minecraft.getMinecraft().gameSettings.keyBindUseItem.getDisplayName().equals("Middle Click"))
				rightClick();
		}
		else if (e.getButton() == 3)
		{
			if (Minecraft.getMinecraft().gameSettings.keyBindAttack.getDisplayName().equals("Button 4"))
				leftClick();
			if (Minecraft.getMinecraft().gameSettings.keyBindUseItem.getDisplayName().equals("Button 4"))
				rightClick();
		}
		else if (e.getButton() == 4)
		{
			if (Minecraft.getMinecraft().gameSettings.keyBindAttack.getDisplayName().equals("Button 5"))
				leftClick();
			if (Minecraft.getMinecraft().gameSettings.keyBindUseItem.getDisplayName().equals("Button 5"))
				rightClick();
		}
		else if (e.getButton() == 5)
		{
			if (Minecraft.getMinecraft().gameSettings.keyBindAttack.getDisplayName().equals("Button 6"))
				leftClick();
			if (Minecraft.getMinecraft().gameSettings.keyBindUseItem.getDisplayName().equals("Button 6"))
				rightClick();
		}
		else if (e.getButton() == 6)
		{
			if (Minecraft.getMinecraft().gameSettings.keyBindAttack.getDisplayName().equals("Button 7"))
				leftClick();
			if (Minecraft.getMinecraft().gameSettings.keyBindUseItem.getDisplayName().equals("Button 7"))
				rightClick();
		}
		else if (e.getButton() == 7)
		{
			if (Minecraft.getMinecraft().gameSettings.keyBindAttack.getDisplayName().equals("Button 8"))
				leftClick();
			if (Minecraft.getMinecraft().gameSettings.keyBindUseItem.getDisplayName().equals("Button 8"))
				rightClick();
		}
		else if (e.getButton() == 8)
		{
			if (Minecraft.getMinecraft().gameSettings.keyBindAttack.getDisplayName().equals("Button 9"))
				leftClick();
			if (Minecraft.getMinecraft().gameSettings.keyBindUseItem.getDisplayName().equals("Button 9"))
				rightClick();
		}
		else if (e.getButton() == 9)
		{
			if (Minecraft.getMinecraft().gameSettings.keyBindAttack.getDisplayName().equals("Button 10"))
				leftClick();
			if (Minecraft.getMinecraft().gameSettings.keyBindUseItem.getDisplayName().equals("Button 10"))
				rightClick();
		}

	}


	@SuppressWarnings("static-access")
	public void rightClick()
	{
		if (SuperClientConfig.display.clickrate.clickrateoptions.intervalRight)
		{
			if (System.currentTimeMillis() -this.lastRightClick <= 5)
				return;
			this.prevLastRightClick = this.lastRightClick;
			this.lastRightClick = System.currentTimeMillis();
		}
		else
			this.rightClicks.add(System.currentTimeMillis());
	}
	/*Apply fog*/
	/*@SubscribeEvent
	public void fog(FogDensity e)
	{
		e.setDensity((float) SuperClientConfig.env.fog_strength); //Custom fog effect
		e.setCanceled(true); //This is necessary to take effect
	}*/

	/**Self explanatory*/
	@SubscribeEvent
	public void zoom(FOVUpdateEvent event)
	{
		if (zooms[0])
			event.setNewfov((float) (event.getFov()-0.5));
		if (zooms[1])
			event.setNewfov((float) (event.getFov()-0.6));
		if (zooms[2])
			event.setNewfov((float) (event.getFov()-0.7));
		if (zooms[3])
			event.setNewfov((float) (event.getFov()-0.8));
		if (zooms[4])
			event.setNewfov((float) (event.getFov()-0.9));
		if (zooms[5])
			event.setNewfov((float) (event.getFov()-1.0));
	}
	/**A newer version uses irritating iterables to get armor items. I use this to make the Iterables actually useful XD*/
	public static ItemStack getStackFromIterable(Iterable<ItemStack> i, int in)
	{
		int index = 0;
		for (ItemStack x: i)
		{
			if (index == in)
				return x;
			index++;
		}
		return ItemStack.EMPTY;
	}
	/**Auto sprint and Auto Sneak applier, and shaders too*/
	@SubscribeEvent
	public void clientTick(ClientTickEvent e)
	{
		if (e.phase != TickEvent.Phase.END)
			return;
		
		if (!SuperClientConfig.shader.equals("") && Minecraft.getMinecraft().player != null)
		{
			if (Minecraft.getMinecraft().entityRenderer.getShaderGroup() == null)
				Minecraft.getMinecraft().entityRenderer.loadShader(new ResourceLocation("shaders/post/"+SuperClientConfig.shader+".json"));
		}
		if (decreasecamdist)
			if (SuperClientEntityRenderer.maximumThirdPersonDistance > 0)
				SuperClientEntityRenderer.maximumThirdPersonDistance -= 0.25;
		if (increasecamdist)
			SuperClientEntityRenderer.maximumThirdPersonDistance += 0.25;
		double change = 1;
		if (activecamslot[6])
			yaw = 0; //reset yaw - 7
		else if (activecamslot[0])
			yaw = yaw + change; //increase yaw - 1
		else if (activecamslot[1])
			yaw = yaw - change; //decrease yaw - 2
		else if (activecamslot[7])
			pitch = 0; //reset pitch - 8
		else if (activecamslot[2])
			pitch = pitch + change; //increase pitch - 3
		else if (activecamslot[3])
			pitch = pitch - change; //decrease pitch - 4
		else if (activecamslot[8])
			roll = 0; //reset roll - 9
		else if (activecamslot[4])
			roll = roll + change; //increase roll - 5
		else if (activecamslot[5])
			roll = roll - change; //decrease roll - 6
		if (SuperClient.toggle)
		{
			EntityPlayerSP p = Minecraft.getMinecraft().player;
			if (p == null)
				return;
			if (sprint)
				KeyBinding.setKeyBindState(Minecraft.getMinecraft().gameSettings.keyBindSprint.getKeyCode(), true);
			else if (!Minecraft.getMinecraft().gameSettings.keyBindSprint.isKeyDown())
				KeyBinding.setKeyBindState(Minecraft.getMinecraft().gameSettings.keyBindSprint.getKeyCode(), false);
			
			if (sneak)
				KeyBinding.setKeyBindState(Minecraft.getMinecraft().gameSettings.keyBindSneak.getKeyCode(), true);
			else if (!Minecraft.getMinecraft().gameSettings.keyBindSneak.isKeyDown())
				KeyBinding.setKeyBindState(Minecraft.getMinecraft().gameSettings.keyBindSneak.getKeyCode(), false);
		}


	}

	@SuppressWarnings("static-access")
	@SubscribeEvent
	public void exit(ClientDisconnectionFromServerEvent e)
	{
		this.menuCommands.clear();
		this.menuSubtitle = "";
		this.menuTitle = "";
		this.currentServer = KnownServer.none;
	}

	@SuppressWarnings("static-access")
	@SubscribeEvent
	public void chat(ClientChatReceivedEvent e)
	{
		if (SuperClientConfig.font.other.nonify)
		{
			String text = e.getMessage().getFormattedText();
			if (!text.contains("Party"))
			{		
				text = text.replace("\u00A7b[MVP] aehmttw\u00A7r\u00A7f:", "\u00A77aehmttw:");
			}
			text = text.replace("\u00A7b[MVP] aehmttw\u00A7r\u00A7f:", "\u00A77aehmttw");
			text = text.replace("\u00A7b[MVP] aehmttw", "\u00A77aehmttw");
			text = text.replace("\u00A7baehmttw", "\u00A77aehmttw");
			if (!e.getMessage().getFormattedText().equals(text))
			{
				e.setMessage(new TextComponentString(text));
			}
		}
		
		//System.out.println(e.getMessage().toString());
		//if (e.getMessage().toString().contains("\u00A7c\u00A7lT\u00A76\u00A7lh\u00A7e\u00A7le \u00a7a\u00A7lO\u00A7b\u00A7lp\u00A7d\u00A7la\u00A7c\u00A7ll \u00A76\u00A7lG\u00A7e\u00A7la\u00A7a\u00A7lm\u00A7b\u00A7le\u00A7d\u00A7ls"))
		if (e.getMessage().toString().contains("TextComponent{text='T', siblings=[], style=Style{hasParent=true, color=\u00A7c, bold=true, italic=null, underlined=null, obfuscated=null, clickEvent=null, hoverEvent=null, insertion=null}}, TextComponent{text='h', siblings=[], style=Style{hasParent=true, color=\u00A76, bold=true, italic=null, underlined=null, obfuscated=null, clickEvent=null, hoverEvent=null, insertion=null}}, TextComponent{text='e ', siblings=[], style=Style{hasParent=true, color=\u00A7e, bold=true, italic=null, underlined=null, obfuscated=null, clickEvent=null, hoverEvent=null, insertion=null}}, TextComponent{text='O', siblings=[], style=Style{hasParent=true, color=\u00A7a, bold=true, italic=null, underlined=null, obfuscated=null, clickEvent=null, hoverEvent=null, insertion=null}}, TextComponent{text='p', siblings=[], style=Style{hasParent=true, color=\u00A7b, bold=true, italic=null, underlined=null, obfuscated=null, clickEvent=null, hoverEvent=null, insertion=null}}, TextComponent{text='a', siblings=[], style=Style{hasParent=true, color=\u00A7d, bold=true, italic=null, underlined=null, obfuscated=null, clickEvent=null, hoverEvent=null, insertion=null}}, TextComponent{text='l ', siblings=[], style=Style{hasParent=true, color=\u00A7c, bold=true, italic=null, underlined=null, obfuscated=null, clickEvent=null, hoverEvent=null, insertion=null}}, TextComponent{text='G', siblings=[], style=Style{hasParent=true, color=\u00A76, bold=true, italic=null, underlined=null, obfuscated=null, clickEvent=null, hoverEvent=null, insertion=null}}, TextComponent{text='a', siblings=[], style=Style{hasParent=true, color=\u00A7e, bold=true, italic=null, underlined=null, obfuscated=null, clickEvent=null, hoverEvent=null, insertion=null}}, TextComponent{text='m', siblings=[], style=Style{hasParent=true, color=\u00A7a, bold=true, italic=null, underlined=null, obfuscated=null, clickEvent=null, hoverEvent=null, insertion=null}}, TextComponent{text='e', siblings=[], style=Style{hasParent=true, color=\u00A7b, bold=true, italic=null, underlined=null, obfuscated=null, clickEvent=null, hoverEvent=null, insertion=null}}, TextComponent{text='s', siblings=[], style=Style{hasParent=true, color=\u00A7d, bold=true, italic=null, underlined=null, obfuscated=null, clickEvent=null, hoverEvent=null, insertion=null}}], style=Style{hasParent=false, color=null, bold=null, italic=null, underlined=null, obfuscated=null, clickEvent=null, hoverEvent=null, insertion=null}"))
		{
			this.currentServer = KnownServer.theopalgames;
			this.menuCommands.clear();
			this.menuTitle = "\u00A7c\u00A7lT\u00A76\u00A7lh\u00A7e\u00A7le \u00a7a\u00A7lO\u00A7b\u00A7lp\u00A7d\u00A7la\u00A7c\u00A7ll \u00A76\u00A7lG\u00A7e\u00A7la\u00A7a\u00A7lm\u00A7b\u00A7le\u00A7d\u00A7ls";
			this.menuSubtitle = "\u00A7bServer menu";
			this.menuCommands.add(new String[]{"Lobby","/server lobby"});
			this.menuCommands.add(new String[]{"The Dawn","/server the-dawn"});
			this.menuCommands.add(new String[]{"Sky Battles","/server sky-battles"});
			this.menuCommands.add(new String[]{"Survival","/server survival"});
			this.menuCommands.add(new String[]{"Arena Battles","/server survival-battle"});
			this.menuCommands.add(new String[]{"Event Server","/server event"});
		}
		if (this.currentServer.equals(KnownServer.theopalgames) && e.getMessage().getFormattedText().contains("\u00A7a(+"))
		{
			Double damage =	Double.parseDouble(e.getMessage().getFormattedText().substring(4,7) + "0");
			if (damage > 1.0)
			{
				this.attackText = e.getMessage().getFormattedText();
				this.attackTime = System.currentTimeMillis();
			}
			e.setCanceled(true);
		}
		else if (this.currentServer.equals(KnownServer.theopalgames) && e.getMessage().getFormattedText().contains("\u00A7c(-"))
		{
			//Double damage =	Double.parseDouble(e.getMessage().toString().split("(+")[1].split(")")[0]);
			this.defenseText = e.getMessage().getFormattedText();
			this.defenseTime = System.currentTimeMillis();
			e.setCanceled(true);
		}
		else if (this.currentServer.equals(KnownServer.theopalgames) && (e.getMessage().getFormattedText().contains("\u00A76Your combo") || e.getMessage().getFormattedText().contains("\u00A76Your chain")))
		{
			this.attackText = "";
			e.setCanceled(true);
		}
	}

	@SuppressWarnings("static-access")
	@SubscribeEvent
	public void renderHud(RenderGameOverlayEvent.Post e)
	{
		Minecraft mc = Minecraft.getMinecraft();

		if (e.getType() == RenderGameOverlayEvent.ElementType.TEXT)
		{
			if (move > 0 && move <= 21 && !boss)
				move--;
		}


		if (e.getType() == RenderGameOverlayEvent.ElementType.BOSSINFO)
		{
			if (move <= 20)
				move = move + 2;
			boss = true;
		}
		if (!(mc.currentScreen instanceof SuperClientEditMenu))
		{
			screen = new ScaledResolution(mc);
			
			width = screen.getScaledWidth();
			height = screen.getScaledHeight();
			
			if (e.getType() == RenderGameOverlayEvent.ElementType.TEXT && SuperClientEditMenu.hud.text.show)
			{
				drawTitle();
			}
			if (e.getType() == RenderGameOverlayEvent.ElementType.TEXT && SuperClientEditMenu.hud.armor.show)
			{
				drawArmor();
			}
			if (e.getType() == RenderGameOverlayEvent.ElementType.TEXT && SuperClientEditMenu.hud.heldItems.show)
			{
				drawHeldItems();
			}
			if (e.getType() == RenderGameOverlayEvent.ElementType.TEXT && SuperClientEditMenu.hud.username.show)
			{
				drawUsername();
			}
			if (e.getType() == RenderGameOverlayEvent.ElementType.TEXT && SuperClientEditMenu.hud.framerate.show)
			{
				drawFPS();
			}
			if (e.getType() == RenderGameOverlayEvent.ElementType.TEXT && SuperClientEditMenu.hud.autoFunctions.show)
			{
				drawAutofunctions();
			}
			if (e.getType() == RenderGameOverlayEvent.ElementType.TEXT && SuperClientEditMenu.hud.clickrateL.show)
			{
				drawClickrateL();
			}
			if (e.getType() == RenderGameOverlayEvent.ElementType.TEXT && SuperClientEditMenu.hud.clickrateR.show)
			{
				drawClickrateR();
			}
			if (e.getType() == RenderGameOverlayEvent.ElementType.TEXT && SuperClientEditMenu.hud.location.show)
			{
				drawLocation();
			}
			if (e.getType() == RenderGameOverlayEvent.ElementType.TEXT && mc.player.getBedLocation() != null && SuperClientEditMenu.hud.spawn.show)
			{
				drawSpawnpoint();
			}
			if (e.getType() == RenderGameOverlayEvent.ElementType.TEXT && SuperClientEditMenu.hud.savedCoords.show)
			{
				drawSavedCoords();
			}
			if (e.getType() == RenderGameOverlayEvent.ElementType.TEXT && SuperClientEditMenu.hud.hotbarNumbers.show)
			{
				drawHotbarNumbers();
			}
			if (e.getType() == RenderGameOverlayEvent.ElementType.TEXT && SuperClientEditMenu.hud.statusEffects.show)
			{
				drawStatusEffects();
			}
			if (e.getType() == RenderGameOverlayEvent.ElementType.TEXT && SuperClientEditMenu.hud.moveControls.show)
			{
				drawMoveControls();
			}
		}
		if (e.getType() == RenderGameOverlayEvent.ElementType.CHAT)
		{
			//System.out.println(SuperClientConfig.saved_coordinates.length);
			/*if (SuperClientEditMenu.hud.text.show)
			{
				int posX = SuperClientEditMenu.hud.text.locX;
				int posY = SuperClientEditMenu.hud.text.locY;
				int offX = SuperClientEditMenu.hud.text.offX;
				int offY = SuperClientEditMenu.hud.text.offY;
				drawStringAtPosition(posX, posY, offX, offY, SuperClientConfig.display.text.text.custom_string, SuperClientConfig.display.text.text.custom_string_color);
				//extramove=12;
				//mc.fontRenderer.drawStringWithShadow(SuperClientConfig.display.text.text.custom_string, width/2-mc.fontRenderer.getStringWidth(SuperClientConfig.display.text.text.custom_string)/2, 1+Math.min(20,move), SuperClientConfig.display.text.text.custom_string_color);
			}*/
			//else
			//extramove=0;

			if (btimer > 0)
			{
				double brightness = mc.gameSettings.gammaSetting;
				String br = "Brightness: " + brightness;
				mc.fontRenderer.drawString(br, width/2-mc.fontRenderer.getStringWidth(br)/2, height/2+12, 0x770077);
				mc.fontRenderer.drawString(br, width/2-mc.fontRenderer.getStringWidth(br)/2-1, height/2+11, getColorFromBrightness(brightness));
				btimer--;
			}
			if (SuperClientInfo.cape_testing)
				mc.fontRenderer.drawString("Test Mode!!", width/2-mc.fontRenderer.getStringWidth("Test Mode!!")/2, height/2-12, 0xff0000);

			if (!SuperClient.instructions.isEmpty())
			{
				if (instCurrent.equals(""))
				{
					if (!SuperClient.instructions.isEmpty())
						instCurrent = SuperClient.instructions.remove(0);
				}
				mc.ingameGUI.drawRect(0, height - 80, width, height - 40, 0x7f000000);
				mc.fontRenderer.drawString(instCurrent, width/2-mc.fontRenderer.getStringWidth(instCurrent)/2, height-70, 0xffffff);
				mc.fontRenderer.drawString("Press "+ keyBindings[19].getDisplayName() + " to continue...", width/2-mc.fontRenderer.getStringWidth("Press " + keyBindings[19].getDisplayName() + " to continue...")/2, height-55, 0xffffff);
			}
			else
			{
				try 
				{
					new File("superclient/tutorial/welcome.yummypie").createNewFile();
					new File("superclient/tutorial/0.7.0.yummypie").createNewFile();
					new File("superclient/tutorial/0.7.4.yummypie").createNewFile();
					new File("superclient/tutorial/0.8.yummypie").createNewFile();
					new File("superclient/tutorial/0.9.yummypie").createNewFile();
					new File("superclient/tutorial/1.0.yummypie").createNewFile();
					
					File updateNotice = new File("superclient/tutorial/updateNotice.yummypie");
					Files.asCharSink(updateNotice, Charsets.UTF_8).write(SuperClient.latestVersion.toString());
				} 
				catch (IOException e1) 
				{
					Throwables.rethrow(e1);
				}
			}
			if (SuperClient.instructions.isEmpty() && showServerMenu)
			{
				int size = this.menuCommands.size() + 1;
				mc.ingameGUI.drawRect(0, height/2 - 20 - size*5, width, height/2 + 20 + size*5, 0x7F000000);
				//mc.ingameGUI.drawRect(/*width/2 - 120*/0, height/2 - size*5 - 15, /*width / 2 + 120*/width, height/2 + size*5 + 15, 0x7f000000/*-1873784752*/);
				int drawHeight = height/2 - size * 5 - 10;
				mc.fontRenderer.drawString(menuTitle, width/2-mc.fontRenderer.getStringWidth(menuTitle)/2, drawHeight, 0xffffff);
				drawHeight += 10;
				mc.fontRenderer.drawString(menuSubtitle, width/2-mc.fontRenderer.getStringWidth(menuSubtitle)/2, drawHeight, 0xffffff);
				drawHeight += 10;

				int i = 0;
				for (String[] command: menuCommands)
				{
					mc.fontRenderer.drawString("[" + mc.gameSettings.keyBindsHotbar[i].getDisplayName() + "] " + command[0], width/2-mc.fontRenderer.getStringWidth("[" + mc.gameSettings.keyBindsHotbar[i].getDisplayName() + "] " + command[0])/2, drawHeight, 0xffffff);
					drawHeight += 10;
					i++;
				}

				mc.fontRenderer.drawString("\u00A7c["+ keyBindings[19].getDisplayName() + "] Exit menu", width/2-mc.fontRenderer.getStringWidth("[" + keyBindings[19].getDisplayName() + "] Exit menu")/2, drawHeight, 0xffffff);
			}
			if (this.currentServer.equals(KnownServer.theopalgames))
			{
				mc.fontRenderer.drawStringWithShadow(this.attackText, width/2 - mc.fontRenderer.getStringWidth(this.attackText) / 2, height - 82, 0xffffff);
				mc.fontRenderer.drawStringWithShadow(this.defenseText, width/2 - mc.fontRenderer.getStringWidth(this.defenseText) / 2, height - 92, 0xffffff);
				if (this.attackText != "")
				{
					//String timer = "";
					int shadedBars = (int) ((System.currentTimeMillis() - this.attackTime) / 50) + 1;
					if (shadedBars > 40)
						shadedBars = 40;

					String bars = "||||||||||||||||||||||||||||||||||||||||";
					String newBars = "\u00A7a" + bars.substring(shadedBars) + "\u00A7c" + bars.substring(40 - shadedBars); 
					this.timerText = newBars;
					mc.fontRenderer.drawString(this.timerText, width/2 - mc.fontRenderer.getStringWidth(this.timerText) / 2, height-62, 0xffffff);
				}
				if (System.currentTimeMillis() - this.defenseTime >= 2000)
					this.defenseText = "";
				if (System.currentTimeMillis() - this.attackTime >= 3000)
					this.attackText = "";

			}

		}
		if (move > 20)
			move = 21;
		boss = false;
		if (e.getType() == RenderGameOverlayEvent.ElementType.CHAT)
		{
			if (showFriends && SuperClient.instructions.isEmpty())
			{
				mc.ingameGUI.drawRect(0, height - 70 - Math.max(onlineFriends.size() * 10, 10), width, height - 40, 0x7F000000);
				mc.fontRenderer.drawString("Friends in view:", width/2-mc.fontRenderer.getStringWidth("Friends in view:")/2, height-60 - Math.max(onlineFriends.size() * 10, 10), 0xffffff);
				if (onlineFriends.size() == 0)
					mc.fontRenderer.drawString("There are no friends within your view", width/2-mc.fontRenderer.getStringWidth("There are no friends within your view")/2, height-55, 0xffffff);
				int move = 0;
				for (int i = 0; i < onlineFriends.size(); i++)
				{
					int x = (int)onlineFriends.get(i).posX;
					int y = (int)onlineFriends.get(i).posY;
					int z = (int)onlineFriends.get(i).posZ;
					if (onlineFriends.get(i).posX < 0)
						x -= 1;
					if (onlineFriends.get(i).posY < 0)
						y -= 1;
					if (onlineFriends.get(i).posZ < 0)
						z -= 1;
					String str = onlineFriends.get(i).getName() + " ( " + x + " / " + y + " / " + z + " )";
					mc.fontRenderer.drawString(str, width/2-mc.fontRenderer.getStringWidth(str)/2, height-55-move, 0xffffff);
					move += 10;
				}
			}
			onlineFriends.clear();
		}
	}
	/////////////
	public void drawArmor()
	{
		int posX = SuperClientEditMenu.hud.armor.locX;
		int posY = SuperClientEditMenu.hud.armor.locY;
		int offX = SuperClientEditMenu.hud.armor.offX;
		int offY = SuperClientEditMenu.hud.armor.offY;
		boolean rightAlign = SuperClientEditMenu.hud.armor.locX == 1;
		if (!rightAlign)
		{
			drawTwoPartString(posX, posY, offX, offY, getArmor(3, false), getColorFromArmor(3), getArmorDurability(3), getColorFromItem(3));
			drawTwoPartString(posX, posY, offX, offY+9, getArmor(2, false), getColorFromArmor(2), getArmorDurability(2), getColorFromItem(2));
			drawTwoPartString(posX, posY, offX, offY+18, getArmor(1, false), getColorFromArmor(1), getArmorDurability(1), getColorFromItem(1));
			drawTwoPartString(posX, posY, offX, offY+27, getArmor(0, false), getColorFromArmor(0), getArmorDurability(0), getColorFromItem(0));
		}
		else
		{
			drawTwoPartString(posX, posY, offX, offY, getArmorDurability(3), getColorFromItem(3), getArmor(3, true), getColorFromArmor(3));
			drawTwoPartString(posX, posY, offX, offY+9, getArmorDurability(2), getColorFromItem(2), getArmor(2, true), getColorFromArmor(2));
			drawTwoPartString(posX, posY, offX, offY+18, getArmorDurability(1), getColorFromItem(1), getArmor(1, true), getColorFromArmor(1));
			drawTwoPartString(posX, posY, offX, offY+27, getArmorDurability(0), getColorFromItem(0), getArmor(0, true), getColorFromArmor(0));
		}
		//mc.fontRenderer.drawStringWithShadow(getArmor(3), 1, 1, getColorFromArmor(3));
		//mc.fontRenderer.drawStringWithShadow(getArmor(2), 1, 10, getColorFromArmor(2));
		//mc.fontRenderer.drawStringWithShadow(getArmor(1), 1, 19, getColorFromArmor(1));
		//mc.fontRenderer.drawStringWithShadow(getArmor(0), 1, 28, getColorFromArmor(0));


		//mc.fontRenderer.drawStringWithShadow(getMainHand(), width/2-mc.fontRenderer.getStringWidth(getMainHand())/2, 1+Math.min(20,move)+extramove, getColorFromItem(mc.player.getHeldItemMainhand()));
		//mc.fontRenderer.drawStringWithShadow(getOffHand(), width/2-mc.fontRenderer.getStringWidth(getOffHand())/2, 10+Math.min(20,move)+extramove, getColorFromItem(mc.player.getHeldItemOffhand()));

	}
	public void drawHeldItems()
	{
		//if (move > 0 && move <= 21 && !boss)
		//	move--;

		int posX = SuperClientEditMenu.hud.heldItems.locX;
		int posY = SuperClientEditMenu.hud.heldItems.locY;
		int offX = SuperClientEditMenu.hud.heldItems.offX;
		int offY = SuperClientEditMenu.hud.heldItems.offY;
		if (SuperClientEditMenu.hud.heldItems.locX != 1)
		{
			drawStringAtPosition(posX, posY, offX, offY, getMainHand(false), getColorFromItem(mc.player.getHeldItemMainhand()));
			drawStringAtPosition(posX, posY, offX, offY + 9, getOffHand(false), getColorFromItem(mc.player.getHeldItemOffhand()));
		}
		else
		{
			drawStringAtPosition(posX, posY, offX, offY, getMainHand(true), getColorFromItem(mc.player.getHeldItemMainhand()));
			drawStringAtPosition(posX, posY, offX, offY + 9, getOffHand(true), getColorFromItem(mc.player.getHeldItemOffhand()));
		}
		//mc.fontRenderer.drawStringWithShadow(getMainHand(), width/2-mc.fontRenderer.getStringWidth(getMainHand())/2, 1+Math.min(20,move)+extramove, getColorFromItem(mc.player.getHeldItemMainhand()));
		//mc.fontRenderer.drawStringWithShadow(getOffHand(), width/2-mc.fontRenderer.getStringWidth(getOffHand())/2, 10+Math.min(20,move)+extramove, getColorFromItem(mc.player.getHeldItemOffhand()));

		//if (move > 20)
		//move = 21;
		//boss = false;
	}
	public void drawTitle()	
	{
		//System.out.println(SuperClientConfig.saved_coordinates.length);

		int posX = SuperClientEditMenu.hud.text.locX;
		int posY = SuperClientEditMenu.hud.text.locY;
		int offX = SuperClientEditMenu.hud.text.offX;
		int offY = SuperClientEditMenu.hud.text.offY;
		drawStringAtPosition(posX, posY, offX, offY, SuperClientConfig.display.text.text.custom_string, SuperClientConfig.display.text.text.custom_string_color);
		//extramove=12;
		//mc.fontRenderer.drawStringWithShadow(SuperClientConfig.display.text.text.custom_string, width/2-mc.fontRenderer.getStringWidth(SuperClientConfig.display.text.text.custom_string)/2, 1+Math.min(20,move), SuperClientConfig.display.text.text.custom_string_color);
	}
	public void drawUsername()
	{
		int posX = SuperClientEditMenu.hud.username.locX;
		int posY = SuperClientEditMenu.hud.username.locY;
		int offX = SuperClientEditMenu.hud.username.offX;
		int offY = SuperClientEditMenu.hud.username.offY;
		drawStringAtPosition(posX, posY, offX, offY, mc.player.getDisplayNameString(), 0xffffff);
		//mc.fontRenderer.drawStringWithShadow(mc.player.getDisplayNameString(), width-mc.fontRenderer.getStringWidth(mc.player.getDisplayNameString())-1, 1, 0xffffff);	
	}
	public void drawFPS()
	{
		int posX = SuperClientEditMenu.hud.framerate.locX;
		int posY = SuperClientEditMenu.hud.framerate.locY;
		int offX = SuperClientEditMenu.hud.framerate.offX;
		int offY = SuperClientEditMenu.hud.framerate.offY;
		String fps = "FPS: " + Minecraft.getDebugFPS();
		drawStringAtPosition(posX, posY, offX, offY, fps, getColorFromFPS(Minecraft.getDebugFPS()));
		//mc.fontRenderer.drawStringWithShadow(fps, width-mc.fontRenderer.getStringWidth(fps)-1, 11, getColorFromFPS(Minecraft.getDebugFPS()));
	}
	public void drawAutofunctions()
	{
		int posX = SuperClientEditMenu.hud.autoFunctions.locX;
		int posY = SuperClientEditMenu.hud.autoFunctions.locY;
		int offX = SuperClientEditMenu.hud.autoFunctions.offX;
		int offY = SuperClientEditMenu.hud.autoFunctions.offY;
		if (sprint)
			drawStringAtPosition(posX, posY, offX, offY, "Autosprinting", 0x00ffff);
		//mc.fontRenderer.drawStringWithShadow("Autosprinting", width-mc.fontRenderer.getStringWidth("Autosprinting")-1, 20, 0x00ffff);
		if (sneak)
			drawStringAtPosition(posX, posY, offX, offY, "Autosneaking", 0xff0000);
		
		if (Minecraft.getMinecraft().currentScreen instanceof SuperClientEditMenu && !sneak && !sprint)
			drawStringAtPosition(posX, posY, offX, offY, "Autofunctions", 0xffffff);
		//mc.fontRenderer.drawStringWithShadow("Autosneaking", width-mc.fontRenderer.getStringWidth("Autosneaking")-1, 20, 0xff0000);
	}
	@SuppressWarnings("static-access")
	public void drawClickrateL()
	{
		int posX = SuperClientEditMenu.hud.clickrateL.locX;
		int posY = SuperClientEditMenu.hud.clickrateL.locY;
		int offX = SuperClientEditMenu.hud.clickrateL.offX;
		int offY = SuperClientEditMenu.hud.clickrateL.offY;
		double attackSpeed = getAttackSpeed(mc.player.getHeldItemMainhand());
		String keybind = mc.gameSettings.keyBindAttack.getDisplayName();
		if (SuperClientConfig.display.clickrate.clickrateoptions.shorthand)
			keybind = parsekey(keybind);

		boolean showSpeed = true;
		if (attackSpeed == -1)
		{
			attackSpeed = 10;
			showSpeed = false;
		}
		
		String speed = " (" + attackSpeed + ")";
		if (!(leftClicks.size() == 0))
		{
			//double round = 1/SuperClientConfig.display.clickrate.clickrateoptions.samplerounding;
			String text;
			String space = "";
			double clickrate = 0;

			if (SuperClientConfig.display.clickrate.clickrateoptions.sampleroundingLeft == 1)
				clickrate = (int)(((int)(1.0/SuperClientConfig.display.clickrate.clickrateoptions.sampleroundingLeft) * (this.leftClicks.size() / (SuperClientConfig.display.clickrate.clickrateoptions.sampletime / 1000.0)))/(1.0/SuperClientConfig.display.clickrate.clickrateoptions.sampleroundingLeft));
			else
				clickrate = ((int)(1.0/SuperClientConfig.display.clickrate.clickrateoptions.sampleroundingLeft) * (this.leftClicks.size() / (SuperClientConfig.display.clickrate.clickrateoptions.sampletime / 1000.0)))/(1.0/SuperClientConfig.display.clickrate.clickrateoptions.sampleroundingLeft);

			if (clickrate < 10 && SuperClientConfig.display.clickrate.clickrateoptions.stabilize)
				space = "0";

			if (!SuperClientConfig.display.clickrate.clickrateoptions.optimal || !showSpeed)
				speed = "";

			if (SuperClientConfig.display.clickrate.clickrateoptions.sampleroundingLeft != 1)
				text = keybind + ": " + space + clickrate + speed;
			else
				text = keybind + ": " + space + (int)clickrate + speed;

			drawStringAtPosition(posX, posY, offX, offY, text, getAttackColorL(clickrate, attackSpeed));

			//mc.fontRenderer.drawStringWithShadow(text, width/2 - mc.fontRenderer.getStringWidth(text)/2, height/2+20, getAttackColor(Math.round(round*(1000.0 / (lastLeftClick-prevLastLeftClick)))/round, attackSpeed));
			for (int i = 0; i < this.leftClicks.size(); i++)
			{
				long l = this.leftClicks.get(i);
				if (l < System.currentTimeMillis() - SuperClientConfig.display.clickrate.clickrateoptions.sampletime)
					this.leftClicks.remove(l);
			}
		}
		else if (SuperClientConfig.display.clickrate.clickrateoptions.intervalLeft && System.currentTimeMillis() - lastLeftClick < SuperClientConfig.display.clickrate.clickrateoptions.sampletime)
		{
			double round = 1/SuperClientConfig.display.clickrate.clickrateoptions.sampleroundingLeft;
			double clickrate = 0;
			String text;
			String space = "";

			if (SuperClientConfig.display.clickrate.clickrateoptions.sampleroundingLeft == 1)
				clickrate = (int)Math.round(round*Math.min(20, 1000.0 / (lastLeftClick-prevLastLeftClick)))/round;
			else
				clickrate = Math.round(round*Math.min(20, 1000.0 / (lastLeftClick-prevLastLeftClick)))/round;

			if (clickrate < 10 && SuperClientConfig.display.clickrate.clickrateoptions.stabilize)
				space = "0";

			if (!SuperClientConfig.display.clickrate.clickrateoptions.optimal || !showSpeed)
				speed = "";

			if (SuperClientConfig.display.clickrate.clickrateoptions.sampleroundingLeft != 1)
				text = keybind + ": " + space + clickrate + speed;
			else
				text = keybind + ": " + space + (int) clickrate + speed;

			//mc.fontRenderer.drawStringWithShadow(text, width/2 - mc.fontRenderer.getStringWidth(text)/2, height/2+20, getAttackColor(Math.round(round*(1000.0 / (lastLeftClick-prevLastLeftClick)))/round, attackSpeed));
			drawStringAtPosition(posX, posY, offX, offY, text, getAttackColorL(clickrate, attackSpeed));

		}
		else if (Minecraft.getMinecraft().currentScreen instanceof SuperClientEditMenu)
			drawStringAtPosition(posX, posY, offX, offY, "Attack Clickrate", 0xffffff);
	}
	@SuppressWarnings("static-access")
	public void drawClickrateR()
	{
		int posX = SuperClientEditMenu.hud.clickrateR.locX;
		int posY = SuperClientEditMenu.hud.clickrateR.locY;
		int offX = SuperClientEditMenu.hud.clickrateR.offX;
		int offY = SuperClientEditMenu.hud.clickrateR.offY;
		String speed = "";
		String keybind = mc.gameSettings.keyBindUseItem.getDisplayName();
		if (SuperClientConfig.display.clickrate.clickrateoptions.shorthand)
			keybind = parsekey(keybind);

		if (mc.player.getHeldItemMainhand().getItem().equals(Items.BOW))
			speed = " (1.0)";
		if (mc.player.getHeldItemOffhand().getItem().equals(Items.BOW))
		{
			Item item = mc.player.getHeldItemMainhand().getItem();
			if (!item.equals(Items.EGG) && !item.equals(Items.SNOWBALL)&& !item.equals(Items.EXPERIENCE_BOTTLE) 
					&& !item.equals(Items.ENDER_EYE) && !item.equals(Items.ENDER_PEARL) && !item.equals(Items.FISHING_ROD))
				speed = " (1.0)";
		}	

		if (!(rightClicks.size() == 0))
		{
			//double round = 1/SuperClientConfig.display.clickrate.clickrateoptions.samplerounding;
			String text;
			String space = "";
			double clickrate = 0;

			if (SuperClientConfig.display.clickrate.clickrateoptions.sampleroundingRight == 1)
				clickrate = (int)(((int)(1.0/SuperClientConfig.display.clickrate.clickrateoptions.sampleroundingRight) * (this.rightClicks.size() / (SuperClientConfig.display.clickrate.clickrateoptions.sampletime / 1000.0)))/(1.0/SuperClientConfig.display.clickrate.clickrateoptions.sampleroundingRight));
			else
				clickrate = ((int)(1.0/SuperClientConfig.display.clickrate.clickrateoptions.sampleroundingRight) * (this.rightClicks.size() / (SuperClientConfig.display.clickrate.clickrateoptions.sampletime / 1000.0)))/(1.0/SuperClientConfig.display.clickrate.clickrateoptions.sampleroundingRight);

			if (clickrate < 10 && SuperClientConfig.display.clickrate.clickrateoptions.stabilize)
				space = "0";

			if (!SuperClientConfig.display.clickrate.clickrateoptions.optimal)
				speed = "";

			if (SuperClientConfig.display.clickrate.clickrateoptions.sampleroundingRight != 1)
				text = keybind + ": " + space + clickrate + speed;
			else
				text = keybind + ": " + space + (int) clickrate + speed;

			int color = getAttackColorR(clickrate);
			if (!speed.equals(""))
				color = getAttackColorR(clickrate, 1);

			drawStringAtPosition(posX, posY, offX, offY, text, color);

			//mc.fontRenderer.drawStringWithShadow(text, width/2 - mc.fontRenderer.getStringWidth(text)/2, height/2+20, getAttackColor(Math.round(round*(1000.0 / (lastLeftClick-prevLastLeftClick)))/round, attackSpeed));
			for (int i = 0; i < this.rightClicks.size(); i++)
			{
				long l = this.rightClicks.get(i);
				if (l < System.currentTimeMillis() - SuperClientConfig.display.clickrate.clickrateoptions.sampletime)
					this.rightClicks.remove(l);
			}
		}
		else if (SuperClientConfig.display.clickrate.clickrateoptions.intervalRight && System.currentTimeMillis() - lastRightClick < SuperClientConfig.display.clickrate.clickrateoptions.sampletime)
		{
			double round = 1/SuperClientConfig.display.clickrate.clickrateoptions.sampleroundingRight;
			double clickrate = 0;
			String text;
			String space = "";

			if (SuperClientConfig.display.clickrate.clickrateoptions.sampleroundingRight == 1)
				clickrate = (int)Math.round(round*Math.min(20, 1000.0 / (lastRightClick-prevLastRightClick)))/round;
			else
				clickrate = Math.round(round*Math.min(20, 1000.0 / (lastRightClick-prevLastRightClick)))/round;

			if (clickrate < 10 && SuperClientConfig.display.clickrate.clickrateoptions.stabilize)
				space = "0";

			if (!SuperClientConfig.display.clickrate.clickrateoptions.optimal)
				speed = "";

			if (SuperClientConfig.display.clickrate.clickrateoptions.sampleroundingRight != 1)
				text = keybind + ": " + space + clickrate + speed;
			else
				text = keybind + ": " + space + (int) clickrate + speed;

			//mc.fontRenderer.drawStringWithShadow(text, width/2 - mc.fontRenderer.getStringWidth(text)/2, height/2+20, getAttackColor(Math.round(round*(1000.0 / (lastLeftClick-prevLastLeftClick)))/round, attackSpeed));

			int color = getAttackColorR(clickrate);
			if (!speed.equals(""))
				color = getAttackColorR(clickrate, 1);

			drawStringAtPosition(posX, posY, offX, offY, text, color);

		}
		else if (Minecraft.getMinecraft().currentScreen instanceof SuperClientEditMenu)
			drawStringAtPosition(posX, posY, offX, offY, "Use Clickrate", 0xffffff);
	}
	public void drawLocation()
	{
		int posX = SuperClientEditMenu.hud.location.locX;
		int posY = SuperClientEditMenu.hud.location.locY;
		int offX = SuperClientEditMenu.hud.location.offX;
		int offY = SuperClientEditMenu.hud.location.offY;
		int x = (int)mc.player.posX;
		int y = (int)mc.player.posY;
		int z = (int)mc.player.posZ;
		if (Minecraft.getMinecraft().player.posX < 0)
			x -= 1;
		if (Minecraft.getMinecraft().player.posY < 0)
			y -= 1;
		if (Minecraft.getMinecraft().player.posZ < 0)
			z -= 1;

		drawStringAtPosition(posX, posY, offX, offY, x + " / " + y + " / " + z, 0xffffff);
		//mc.fontRenderer.drawStringWithShadow(x + " / " + y + " / " + z, width-mc.fontRenderer.getStringWidth(x + " / " + y + " / " + z)-1, height-9, 0xffffff);
	}
	public void drawSpawnpoint()
	{
		int posX = SuperClientEditMenu.hud.spawn.locX;
		int posY = SuperClientEditMenu.hud.spawn.locY;
		int offX = SuperClientEditMenu.hud.spawn.offX;
		int offY = SuperClientEditMenu.hud.spawn.offY;
		int x1 = (int)mc.player.getBedLocation().getX();
		int y1 = (int)mc.player.getBedLocation().getY();
		int z1 = (int)mc.player.getBedLocation().getZ();

		drawStringAtPosition(posX, posY, offX, offY, "Spawn: " + x1 + " / " + y1 + " / " + z1, 0X7F7F7F);
		//mc.fontRenderer.drawStringWithShadow("Spawn: " + x1 + " / " + y1 + " / " + z1, width-mc.fontRenderer.getStringWidth("Spawn: " + x1 + " / " + y1 + " / " + z1)-1, height-19, 0x7f7f7f);
	}
	public void drawSavedCoords()
	{
		int posX = SuperClientEditMenu.hud.savedCoords.locX;
		int posY = SuperClientEditMenu.hud.savedCoords.locY;
		int offX = SuperClientEditMenu.hud.savedCoords.offX;
		int offY = SuperClientEditMenu.hud.savedCoords.offY;
		if (posY == -1)
			offY -= 9 ;
		else if (posY == 0)
			offY -= 45;
		else if (posY == 1)
			offY -= 80;
		int[][] coords = new int[9][3];
		for (int i = 0; i < 9; i++)
			coords[i] = getCoords(i);
		int offset = 0;
		for (int i = 0; i < coords.length; i++)
		{
			offset += 9;
			if (coords[i][1]==Integer.MIN_VALUE)
			{
				if (Minecraft.getMinecraft().currentScreen instanceof SuperClientEditMenu)
				{
					if (posX == 1)
						drawStringAtPosition(posX, posY, offX, offY + offset, "Coordinate Slot [" + (i + 1) + "]" , getGradientColor(false)[8-i%9]);
					else
						drawStringAtPosition(posX, posY, offX, offY + offset, "[" + (i + 1) + "] Coordinate Slot" , getGradientColor(false)[8-i%9]);
					
				}
				continue;
			}
			if (posX == 1)
				drawStringAtPosition(posX, posY, offX, offY + offset, coords[i][0] + " / " + coords[i][1] + " / " + coords[i][2] + " [" + (i+1) +"]", getGradientColor(false)[8-i%9]);
			else
				drawStringAtPosition(posX, posY, offX, offY + offset, "[" + (i+1) +"] " +  coords[i][0] + " / " + coords[i][1] + " / " + coords[i][2], getGradientColor(false)[8-i%9]);

			//mc.fontRenderer.drawStringWithShadow(coords[i][0] + " / " + coords[i][1] + " / " + coords[i][2] + " [" + (i+1) +"]", width-1-mc.fontRenderer.getStringWidth(coords[i][0] + " / " + coords[i][1] + " / " + coords[i][2] + " [" + (i+1) + "]"), height/2-40+i*9, getHotbarColor()[8-i%9]);
		}
	}
	public void drawHotbarNumbers()
	{
		if (!mc.player.isSpectator())
		{
			int h = 26;
			if (mc.player.capabilities.isCreativeMode)
				h = 30;

			int[] cols = getGradientColor(true);

			int drawY = -80;
			for (int n = 0; n < 9; n++)
			{
				mc.fontRenderer.drawStringWithShadow(parsekey(mc.gameSettings.keyBindsHotbar[n].getDisplayName()), width/2+drawY-mc.fontRenderer.getStringWidth(parsekey(mc.gameSettings.keyBindsHotbar[n].getDisplayName()))/2/*+SuperClientEditMenu.hud.hotbarNumbers.offX*/, height-h/*+SuperClientEditMenu.hud.hotbarNumbers.offY*/, cols[8-n]);
				drawY += 20;
			}
		}
		/*mc.fontRenderer.drawStringWithShadow(parsekey(mc.gameSettings.keyBindsHotbar[0].getDisplayName()), width/2-83, height-h, 0xff7f7f);
			mc.fontRenderer.drawStringWithShadow(parsekey(mc.gameSettings.keyBindsHotbar[1].getDisplayName()), width/2-63, height-h, 0xffaa7f);
			mc.fontRenderer.drawStringWithShadow(parsekey(mc.gameSettings.keyBindsHotbar[2].getDisplayName()), width/2-43, height-h, 0xffff7f);
			mc.fontRenderer.drawStringWithShadow(parsekey(mc.gameSettings.keyBindsHotbar[3].getDisplayName()), width/2-23, height-h, 0x7fff7f);
			mc.fontRenderer.drawStringWithShadow(parsekey(mc.gameSettings.keyBindsHotbar[4].getDisplayName()), width/2-3, height-h, 0x7fffff);
			mc.fontRenderer.drawStringWithShadow(parsekey(mc.gameSettings.keyBindsHotbar[5].getDisplayName()), width/2+17, height-h, 0x7faaff);
			mc.fontRenderer.drawStringWithShadow(parsekey(mc.gameSettings.keyBindsHotbar[6].getDisplayName()), width/2+37, height-h, 0x7f7fff);
			mc.fontRenderer.drawStringWithShadow(parsekey(mc.gameSettings.keyBindsHotbar[7].getDisplayName()), width/2+57, height-h, 0xaa7fff);
			mc.fontRenderer.drawStringWithShadow(parsekey(mc.gameSettings.keyBindsHotbar[8].getDisplayName()), width/2+77, height-h, 0xff7fff);
		 */
	}
	public void drawStatusEffects()	
	{
		int posX = SuperClientEditMenu.hud.statusEffects.locX;
		int posY = SuperClientEditMenu.hud.statusEffects.locY;
		int offX = SuperClientEditMenu.hud.statusEffects.offX;
		int offY = SuperClientEditMenu.hud.statusEffects.offY;
		int effects = 28;
		int drawY = 0;
		int deltaY = 9;
		if (posY == -1)
		{
			drawY = offY;
		}
		else if (posY == 0)
		{
			drawY = height/2 - 5 + offY;
		}
		else if (posY == 1)
		{
			drawY = height - 9 + offY;
			deltaY = -9;
		}
		int total = 0;
		for (int i = effects; i > 0; i--)
		{
			PotionEffect p = mc.player.getActivePotionEffect(Potion.getPotionById(i));
			if (p != null)
			{
				total++;
				String a;
				if (p.getAmplifier()==0)
					a = "";
				else
					a = " ";
				
				int color = getColorFromFPS(p.getDuration()/20.0);
				
				if (SuperClientEditMenu.hud.statusEffects.locX != 1)
				{
					if (p.getDuration()/20%60 >= 10)
						drawTwoPartString(posX, -1, offX, drawY, I18n.translateToLocal(p.getEffectName()) + a + I18n.translateToLocal("potion.potency."+(p.getAmplifier())) + " - ", p.getPotion().getLiquidColor(), p.getDuration()/20/60 + ":" + p.getDuration()/20%60, color);
					else
						drawTwoPartString(posX, -1, offX, drawY, I18n.translateToLocal(p.getEffectName()) + a + I18n.translateToLocal("potion.potency."+(p.getAmplifier())) + " - ", p.getPotion().getLiquidColor(), + p.getDuration()/20/60 + ":0" + p.getDuration()/20%60, color);
				}
				else
				{
					if (p.getDuration()/20%60 >= 10)
						drawTwoPartString(posX, -1, offX, drawY, p.getDuration()/20/60 + ":" + p.getDuration()/20%60, color, " - " + I18n.translateToLocal(p.getEffectName()) + a + I18n.translateToLocal("potion.potency."+(p.getAmplifier())), p.getPotion().getLiquidColor());
					else
						drawTwoPartString(posX, -1, offX, drawY,  + p.getDuration()/20/60 + ":0" + p.getDuration()/20%60, color, " - " + I18n.translateToLocal(p.getEffectName()) + a + I18n.translateToLocal("potion.potency."+(p.getAmplifier())), p.getPotion().getLiquidColor());
				}
				drawY = drawY+deltaY;
			}
		}
		if (total == 0)
			if (Minecraft.getMinecraft().currentScreen instanceof SuperClientEditMenu)
				drawStringAtPosition(posX, -1, offX, drawY, "Status Effects", 0xffffff);
	}
	public void drawMoveControls()
	{
		int posX = SuperClientEditMenu.hud.moveControls.locX;
		int posY = SuperClientEditMenu.hud.moveControls.locY;
		int offX = SuperClientEditMenu.hud.moveControls.offX;
		int offY = SuperClientEditMenu.hud.moveControls.offY;

		int locX = 0;
		int locY = 0;

		if (posX == -1)
			locX = 0;
		else if (posX == 0)
			locX = width / 2;
		else if (posX == 1)
			locX = width;

		if (posY == -1)
			locY = 0;
		else if (posY == 0)
			locY = height / 2;
		else if (posY == 1)
			locY = height;

		String forward = parsekey(mc.gameSettings.keyBindForward.getDisplayName());
		String back = parsekey(mc.gameSettings.keyBindBack.getDisplayName());
		String left = parsekey(mc.gameSettings.keyBindLeft.getDisplayName());
		String right = parsekey(mc.gameSettings.keyBindRight.getDisplayName());
		String jump = parsekey(mc.gameSettings.keyBindJump.getDisplayName());
		Boolean pforward = mc.gameSettings.keyBindForward.isKeyDown();
		Boolean pback = mc.gameSettings.keyBindBack.isKeyDown();
		Boolean pleft = mc.gameSettings.keyBindLeft.isKeyDown();
		Boolean pright = mc.gameSettings.keyBindRight.isKeyDown();
		Boolean pjump = mc.gameSettings.keyBindJump.isKeyDown();

		int m = 4;

		
		if (pforward)
		{
			if (col1 < 0x7ffd7f)
				col1+=0x020402*m;
		}
		else if (col1 > 0)
			col1-=0x010201*m;
		if (pback)
		{
			if (col2 < 0xfd7f7f)
				col2+=0x040202*m;
		}
		else if (col2 > 0)
			col2-=0x020101*m;
		if (pright)
		{
			if (col3 < 0x7f7ffd)
				col3+=0x020204*m;
		}
		else if (col3 > 0)
			col3-=0x010102*m;
		if (pleft)
		{
			if (col4 < 0xfdfd7f)
				col4+=0x040402*m;
		}
		else if (col4 > 0)
			col4-=0x020201*m;
		if (pjump)
		{
			if (col5 < 0xfdfdfd)
				col5+=0x040404*m;
		}
		else if (col5 > 0)
			col5-=0x020202*m;

		col1 = Math.min(col1, 0x7ffd7f);
		col2 = Math.min(col2, 0xfd7f7f);
		col3 = Math.min(col3, 0x7f7ffd);
		col4 = Math.min(col4, 0xfdfd7f);
		col5 = Math.min(col5, 0xfdfdfd);
		col1 = Math.max(col1, 0);
		col2 = Math.max(col2, 0);
		col3 = Math.max(col3, 0);
		col4 = Math.max(col4, 0);
		col5 = Math.max(col5, 0);
		
		if (!pforward && SuperClientConfig.font.other.rainbowmode)
			col1 = 0;
		if (!pback && SuperClientConfig.font.other.rainbowmode)
			col2 = 0;
		if (!pright && SuperClientConfig.font.other.rainbowmode)
			col3 = 0;
		if (!pleft && SuperClientConfig.font.other.rainbowmode)
			col4 = 0;
		if (!pjump && SuperClientConfig.font.other.rainbowmode)
			col5 = 0;
		
		int o1 = 0;
		int o2 = 0;

		if (!left.equals("\u25C0"))
			o1 = 1;
		if (!right.equals("\u25B6"))
			o2 = 1;
		//drawStringAtPosition(posX, posY, offX, offY-27, forward, col1);
		//drawStringAtPosition(posX, posY, o1+offX-6, offY-18, left, col4);
		//drawStringAtPosition(posX, posY, offX-3, offY-18, jump, col5);
		//drawStringAtPosition(posX, posY, -o2+offX, offY-18, right, col3);
		//drawStringAtPosition(posX, posY, offX, offY-9, back, col2);


		mc.fontRenderer.drawString(forward, offX+locX-mc.fontRenderer.getStringWidth(forward)/2-mc.fontRenderer.getStringWidth(jump)/2-mc.fontRenderer.getStringWidth(right), locY-18+offY, col1);
		mc.fontRenderer.drawString(left, o1+offX+locX-3-mc.fontRenderer.getStringWidth(right)-mc.fontRenderer.getStringWidth(jump)-mc.fontRenderer.getStringWidth(left), locY-9+offY, col4);
		mc.fontRenderer.drawString(jump, offX+locX-mc.fontRenderer.getStringWidth(right)-mc.fontRenderer.getStringWidth(jump), locY-9+offY, col5);
		mc.fontRenderer.drawString(right, -o2+offX+locX+3-mc.fontRenderer.getStringWidth(right), locY-9+offY, col3);
		mc.fontRenderer.drawString(back, offX+locX-mc.fontRenderer.getStringWidth(back)/2-mc.fontRenderer.getStringWidth(jump)/2-mc.fontRenderer.getStringWidth(right), locY-0+offY, col2);

	}



	/////////////
	public static String parsekey(String s)
	{
		if (s.equals("SPACE"))
			return "\u2742";
		if (s.equals("RMENU"))
			return "\u274B";
		if (s.equals("LMENU"))
			return "\u273B";
		if (s.equals("RMETA") || s.equals("RCONTROL"))
			return "\u274B";
		if (s.equals("LMETA"))
			return "\u2736";
		if (s.equals("LCONTROL"))
			return "\u2735";
		if (s.equals("UP"))
			return "\u25B2";
		if (s.equals("DOWN"))
			return "\u25BC";
		if (s.equals("LEFT"))
			return "\u25C0";
		if (s.equals("RIGHT"))
			return "\u25B6";
		if (s.equals("MINUS"))
			return "-";
		if (s.equals("EQUALS"))
			return "=";
		if (s.equals("END"))
			return "\u273E";
		if (s.equals("BACK"))
			return "\u2717";
		if (s.equals("LBRACKET"))
			return "[";
		if (s.equals("RBRACKET"))
			return "]";
		if (s.equals("COMMA"))
			return ",";
		if (s.equals("APOSTROPHE"))
			return "'";
		if (s.equals("GRAVE"))
			return "`";
		if (s.equals("SLASH"))
			return "/";
		if (s.equals("BACKSLASH"))
			return "\\";
		if (s.equals("Left Click"))
			return "*1";
		if (s.equals("Right Click"))
			return "*2";
		if (s.equals("Middle Click"))
			return "*3";
		if (s.equals("Button 4"))
			return "*4";
		if (s.equals("Button 5"))
			return "*5";
		if (s.equals("Button 6"))
			return "*6";
		if (s.equals("Button 7"))
			return "*7";
		if (s.equals("Button 8"))
			return "*8";
		if (s.equals("Button 9"))
			return "*9";
		if (s.equals("Button 10"))
			return "*10";

		else return s;
	}
	
	public static String getArmor(int slot, boolean rightAlign)
	{
		String pref = "";
		String suf = " - ";
		if (rightAlign)
		{
			pref = " - ";
			suf = "";
		}
			
		if (getStackFromIterable(Minecraft.getMinecraft().player.getArmorInventoryList(), slot).getCount() == 0)
		{
			if (Minecraft.getMinecraft().currentScreen instanceof SuperClientEditMenu)
			{
				if (slot == 3)
					return "Helmet Slot";
				if (slot == 2)
					return "Chestplate Slot";
				if (slot == 1)
					return "Leggings Slot";
				if (slot == 0)
					return "Boots Slot";
			}
			return "";
		}
		if (getStackFromIterable(Minecraft.getMinecraft().player.getArmorInventoryList(), slot).getMaxDamage() == 0)
			return getStackFromIterable(Minecraft.getMinecraft().player.getArmorInventoryList(), slot).getDisplayName();

		return pref + getStackFromIterable(Minecraft.getMinecraft().player.getArmorInventoryList(), slot).getDisplayName() + suf;
	}
	public static String getArmorDurability(int slot)
	{
		if (getStackFromIterable(Minecraft.getMinecraft().player.getArmorInventoryList(), slot).getCount() == 0)
			return "";

		if (getStackFromIterable(Minecraft.getMinecraft().player.getArmorInventoryList(), slot).getMaxDamage() == 0)
			return "";

		return (getStackFromIterable(Minecraft.getMinecraft().player.getArmorInventoryList(), slot).getMaxDamage() - getStackFromIterable(Minecraft.getMinecraft().player.getArmorInventoryList(), slot).getItemDamage()) + " / " + getStackFromIterable(Minecraft.getMinecraft().player.getArmorInventoryList(), slot).getMaxDamage();
	}
	public static String getMainHand(boolean rightAlign)
	{
		if (Minecraft.getMinecraft().player.getHeldItemMainhand().getCount() == 0)
		{
			if (Minecraft.getMinecraft().currentScreen instanceof SuperClientEditMenu)
				return "Main Hand Slot";
			return "";
		}
		if (!rightAlign)
		{
			if (Minecraft.getMinecraft().player.getHeldItemMainhand().getCount() > 1)
				return Minecraft.getMinecraft().player.getHeldItemMainhand().getDisplayName() + " x" + Minecraft.getMinecraft().player.getHeldItemMainhand().getCount();
			if (Minecraft.getMinecraft().player.getHeldItemMainhand().getMaxDamage() == 0)
				return Minecraft.getMinecraft().player.getHeldItemMainhand().getDisplayName();
		
			return Minecraft.getMinecraft().player.getHeldItemMainhand().getDisplayName() + " - " + (Minecraft.getMinecraft().player.getHeldItemMainhand().getMaxDamage() - Minecraft.getMinecraft().player.getHeldItemMainhand().getItemDamage() + " / " + Minecraft.getMinecraft().player.getHeldItemMainhand().getMaxDamage());
		}
		else
		{
			if (Minecraft.getMinecraft().player.getHeldItemMainhand().getCount() > 1)
				return Minecraft.getMinecraft().player.getHeldItemMainhand().getCount() + "x " + Minecraft.getMinecraft().player.getHeldItemMainhand().getDisplayName();
			if (Minecraft.getMinecraft().player.getHeldItemMainhand().getMaxDamage() == 0)
				return Minecraft.getMinecraft().player.getHeldItemMainhand().getDisplayName();
		
			return (Minecraft.getMinecraft().player.getHeldItemMainhand().getMaxDamage() - Minecraft.getMinecraft().player.getHeldItemMainhand().getItemDamage() + " / " + Minecraft.getMinecraft().player.getHeldItemMainhand().getMaxDamage()) + " - " + Minecraft.getMinecraft().player.getHeldItemMainhand().getDisplayName();
		}
	}
	public static String getOffHand(boolean rightAlign)
	{
		if (Minecraft.getMinecraft().player.getHeldItemOffhand().getCount() == 0)
		{
			if (Minecraft.getMinecraft().currentScreen instanceof SuperClientEditMenu)
				return "Off Hand Slot";
			return "";
		}
		if (!rightAlign)
		{
			if (Minecraft.getMinecraft().player.getHeldItemOffhand().getCount() > 1)
				return Minecraft.getMinecraft().player.getHeldItemOffhand().getDisplayName() + " x" + Minecraft.getMinecraft().player.getHeldItemOffhand().getCount();
			if (Minecraft.getMinecraft().player.getHeldItemOffhand().getMaxDamage() == 0)
				return Minecraft.getMinecraft().player.getHeldItemOffhand().getDisplayName();
		
			return Minecraft.getMinecraft().player.getHeldItemOffhand().getDisplayName() + " - " + (Minecraft.getMinecraft().player.getHeldItemOffhand().getMaxDamage() - Minecraft.getMinecraft().player.getHeldItemOffhand().getItemDamage() + " / " + Minecraft.getMinecraft().player.getHeldItemOffhand().getMaxDamage());
		}
		else
		{
			if (Minecraft.getMinecraft().player.getHeldItemOffhand().getCount() > 1)
				return Minecraft.getMinecraft().player.getHeldItemOffhand().getCount() + "x " + Minecraft.getMinecraft().player.getHeldItemOffhand().getDisplayName();
			if (Minecraft.getMinecraft().player.getHeldItemOffhand().getMaxDamage() == 0)
				return Minecraft.getMinecraft().player.getHeldItemOffhand().getDisplayName();
		
			return (Minecraft.getMinecraft().player.getHeldItemOffhand().getMaxDamage() - Minecraft.getMinecraft().player.getHeldItemOffhand().getItemDamage() + " / " + Minecraft.getMinecraft().player.getHeldItemOffhand().getMaxDamage()) + " - " + Minecraft.getMinecraft().player.getHeldItemOffhand().getDisplayName();
		}
	}
	public static int getColorFromItem(ItemStack i)
	{
		if (i.getMaxDamage() == 0)
			return 0xffffff;
		return getColorFromFPS(60*(i.getMaxDamage()-i.getItemDamage())/(i.getMaxDamage()*1.0));
	}
	public static int getColorFromItem(int slot)
	{
		ItemStack i = getStackFromIterable(Minecraft.getMinecraft().player.getArmorInventoryList(), slot);

		if (i.getMaxDamage() == 0)
			return 0xffffff;
		return getColorFromFPS(60*(i.getMaxDamage()-i.getItemDamage())/(i.getMaxDamage()*1.0));
	}
	public static int getColorFromFPS(double fps)
	{
		if (fps < 0)
			return 255*256*256;
		int red = 255;
		if (fps > 20 && fps <= 120)
			red = (int)(Math.max(0, 255*(40 - fps)/20.0));
		else if (fps > 120 && fps < 240)
			red = 255 - (int)(Math.min(255, 255*((240-fps)/120.0)));
		else if (fps >= 240)
			red = 255;
		int green = 0;
		if (fps < 20)
			green = (int)(255*(fps / 20.0));
		else if (fps >= 20 && fps <= 60)
			green = 255;
		else if (fps > 60 && fps < 120)
			green = (int)(255 - 255*((fps - 60)/60.0));
		else if (fps > 240 && fps < 495)
			green = (int)fps - 240;
		else if (fps >= 495)
			green = 255;
		int blue = 0;
		if (fps < 60 && fps > 40)
			blue = (int)(255*((fps - 40) / 20.0));
		else if (fps >= 60)
			blue = 255;

		return blue + green*256+ red*256*256;
	}
	public static int getColorFromBrightness(double b)
	{
		int value = (int)( 255*b/7);
		if (b < 0)
			return 0;
		if (b > 7)
			value = 255;
		return value + value*256+ value*256*256;
	}
	public static int getColorFromArmor(int l)
	{
		ItemStack armor = getStackFromIterable(Minecraft.getMinecraft().player.getArmorInventoryList(), l);
		if (armor.getItem().equals(Items.DIAMOND_HELMET) ||
				armor.getItem().equals(Items.DIAMOND_CHESTPLATE) ||
				armor.getItem().equals(Items.DIAMOND_LEGGINGS) ||
				armor.getItem().equals(Items.DIAMOND_BOOTS)  )
		{
			return parseColor(SuperClientConfig.display.armor.colors.armor_color_diamond)/*0x33ebcb*/;
		}
		if (armor.getItem().equals(Items.IRON_HELMET) ||
				armor.getItem().equals(Items.IRON_CHESTPLATE) ||
				armor.getItem().equals(Items.IRON_LEGGINGS) ||
				armor.getItem().equals(Items.IRON_BOOTS)  )
		{
			return parseColor(SuperClientConfig.display.armor.colors.armor_color_iron)/*0xc6c6c6*/;
		}
		if (armor.getItem().equals(Items.CHAINMAIL_HELMET) ||
				armor.getItem().equals(Items.CHAINMAIL_CHESTPLATE) ||
				armor.getItem().equals(Items.CHAINMAIL_LEGGINGS) ||
				armor.getItem().equals(Items.CHAINMAIL_BOOTS)  )
		{
			return parseColor(SuperClientConfig.display.armor.colors.armor_color_chainmail)/*0x6d6d6d*/;
		}
		if (armor.getItem().equals(Items.LEATHER_HELMET) ||
				armor.getItem().equals(Items.LEATHER_CHESTPLATE) ||
				armor.getItem().equals(Items.LEATHER_LEGGINGS) ||
				armor.getItem().equals(Items.LEATHER_BOOTS)  )
		{
			return parseColor(SuperClientConfig.display.armor.colors.armor_color_leather)/*0x72482e*/;
		}
		if (armor.getItem().equals(Items.GOLDEN_HELMET) ||
				armor.getItem().equals(Items.GOLDEN_CHESTPLATE) ||
				armor.getItem().equals(Items.GOLDEN_LEGGINGS) ||
				armor.getItem().equals(Items.GOLDEN_BOOTS)  )
		{
			return parseColor(SuperClientConfig.display.armor.colors.armor_color_gold)/*0xeaee57*/;
		}
		if (armor.getItem().equals(Items.ELYTRA))
			return parseColor(SuperClientConfig.display.armor.colors.armor_color_elytra)/*0xd2b4dd*/;
		if (armor.getItem().equals(Item.getItemFromBlock(Blocks.PUMPKIN)))
			return parseColor(SuperClientConfig.display.armor.colors.armor_color_pumpkin)/*0xe3901d*/;
		else
			return parseColor(SuperClientConfig.display.armor.colors.armor_color_default);
	}
	public static int parseColor(int color)
	{
		if (!SuperClientConfig.display.armor.colors.use_RRRGGGBBB_armor)
			return color;
		else
			return color/1000000*256*256+((color/1000)%1000)*256+(color%1000);
	}
	public static int parseTextColor(int color)
	{
		if (!SuperClientConfig.font.colorcodes.use_RRRGGGBBB_text)
			return color;
		else
			return color/1000000*256*256+((color/1000)%1000)*256+(color%1000);
	}
	public static int[] getGradientColor(boolean hotbar)
	{
		int[] color;
		if (hotbar)
			color = SuperClientConfig.display.hotbarnumbers.colors.hotbar_colors;
		else
			color = SuperClientConfig.display.coords.colors.coord_colors;
		if (color.length <= 0)
			return new int[]{0,0,0,0,0,0,0,0,0};
		int[][] colors = new int[color.length][3];

		boolean rrrgggbbb;
		if (hotbar)
			rrrgggbbb = SuperClientConfig.display.hotbarnumbers.colors.use_RRRGGGBBB;
		else
			rrrgggbbb = SuperClientConfig.display.coords.colors.use_RRRGGGBBB;

		if (!rrrgggbbb)
			for (int i = 0; i < color.length; i++)
			{
				colors[colors.length-1-i][0] = color[i]/256/256;
				colors[colors.length-1-i][1] = (color[i]/256)%256;
				colors[colors.length-1-i][2] = (color[i])%256;
				//System.out.println(colors[i][0] + " " + colors[i][1] + " " + colors[i][2]);
			}
		else
			for (int i = 0; i < color.length; i++)
			{
				colors[colors.length-1-i][0] = Math.min(color[i]/1000000, 255);
				colors[colors.length-1-i][1] = Math.min((color[i]/1000)%1000, 255);
				colors[colors.length-1-i][2] = Math.min(color[i]%1000, 255);
			}
		int[][] newcolors = new int[9][3];

		double weightdist = (double)(colors.length-1)/(double)(newcolors.length-1);
		for (int i = 0; i < 9; i++)
		{
			for (int j = 0; j < 3; j++)
			{
				if (i == 0)
				{
					newcolors[i][j] = colors[0][j];
				}
				else if (i < 8)
				{
					newcolors[i][j] = (int)((colors[(int)(i * weightdist+1)][j]*(weightdist*i-(int)(weightdist*i))) + (colors[(int)(i * weightdist)][j]*(-weightdist*i+1+(int)(weightdist*i))));
					//newcolors[8-i][j] /= 2;
				}
				else
					newcolors[i][j] = colors[colors.length-1][j];

			}
		}
		int[] result = new int[9];
		for (int i = 0; i < 9; i++)
		{
			result[i] = newcolors[i][0]*256*256 + newcolors[i][1]*256 + newcolors[i][2];
		}
		return result;
	}
	public static void addCoords(int x, int y, int z, int number)
	{
		try
		{
			printwriter = new PrintWriter(new OutputStreamWriter(new FileOutputStream("superclient/coords/"+SuperClientConfig.display.coords.saved_coordinate_profile+"/"+number + "_coords.yummypie"), StandardCharsets.UTF_8));
			printwriter.println(x + "_" + y + "_" + z /*+ "|" + world + "\uabcd" + dim*/);
			printwriter.close();

		} catch (FileNotFoundException e) {
			for (int a = 0; a < 9; a++)
				try 
				{
					new File("superclient/coords/"+SuperClientConfig.display.coords.saved_coordinate_profile + "/" + a + "_coords.yummypie").createNewFile();
				}
				catch (IOException e1) 
				{
					e1.printStackTrace();
				}
		}

	}
	public static void loadCoords()
	{
		String folder = "default";
		try
		{
			folder = SuperClientConfig.display.coords.saved_coordinate_profile;
		}
		catch (Exception e) {};
		new File("superclient/coords/"+folder).mkdirs();
		for (int number = 0; number < 9; number++)
		{


			if (!(new File("superclient/coords/"+folder+"/"+number+"_coords.yummypie").exists()))
			{
				coordinates[number] = new int[]{0, Integer.MIN_VALUE, 0};

			}
			try
			{
				List<String> list = IOUtils.readLines(new FileInputStream("superclient/coords/"+folder+"/"+number + "_coords.yummypie"));
				int[] coords = new int[3];
				//int index = 0;
				for (String s : list)
				{
					//String[] y = s.split("|")[1].split("\uabcd");
					String[] z = s/*.split("|")[0]*/.split("_");

					//if (y[0].equals(Minecraft.getMinecraft().world.toString()) && y[1].equals(Minecraft.getMinecraft().player.dimension))
					{
						coords[0] = Integer.parseInt(z[0]);
						coords[1] = Integer.parseInt(z[1]);
						coords[2] = Integer.parseInt(z[2]);
						//index++;
					}
				}
				coordinates[number] = coords;

			} catch (IOException e) {
				//e.printStackTrace();
			}

		}
	}
	public static int[] getCoords(int number)
	{
		return coordinates[number];
	}
	public double getAttackSpeed(ItemStack item)
	{
		@SuppressWarnings("rawtypes")
		List tooltips = item.getTooltip(Minecraft.getMinecraft().player, ITooltipFlag.TooltipFlags.NORMAL);
		for (int i = 0; i < tooltips.size(); i++)
		{
			if (((String)(tooltips.get(i))).contains(I18n.translateToLocal("attribute.name.generic.attackSpeed")))
			{
				try
				{
					String[] parts = ((String)(tooltips.get(i))).split(" ");
					return Double.parseDouble(parts[1]);
				}
				catch (Exception e)
				{
					return -1;
				}
			}
		}
		return 4.0;
	}
	public double getAttackSpeed(Item item)
	{
		double x = 0;
		double y = 0;
		if(item.getAttributeModifiers(EntityEquipmentSlot.MAINHAND, new ItemStack(item)).containsKey("generic.attackSpeed")){
			for(AttributeModifier mod : item.getAttributeModifiers(EntityEquipmentSlot.MAINHAND, new ItemStack(item)).get("generic.attackSpeed"))
			{
				if(mod.getOperation() == 0)
				{
					x += mod.getAmount();
				}
			}

			y = x;

			for(AttributeModifier mod : item.getAttributeModifiers(EntityEquipmentSlot.MAINHAND, new ItemStack(item)).get("generic.attackSpeed"))
			{
				if(mod.getOperation() == 1)
				{
					y += x * mod.getAmount();
				}
			}

			for(AttributeModifier mod : item.getAttributeModifiers(EntityEquipmentSlot.MAINHAND, new ItemStack(item)).get("generic.attackSpeed"))
			{
				if(mod.getOperation() == 2)
				{
					y += y * mod.getAmount();
				}
			}
		}

		return Math.round((y + 4)*100)/100.0;
	}
	public int getAttackColorL(double input, double perfect)
	{
		if (SuperClientConfig.display.clickrate.clickrateoptions.sampleroundingLeft <= 0.1)
			return getColorFromFPS(60-(Math.abs((input - perfect) / perfect))*100);
		else
			return 0xffffff;
	}
	public int getAttackColorL(double input)
	{
		if (SuperClientConfig.display.clickrate.clickrateoptions.sampleroundingLeft <= 0.1)
			return getColorFromFPS(input*6);
		else
			return 0xffffff;
	}
	public int getAttackColorR(double input, double perfect)
	{
		if (SuperClientConfig.display.clickrate.clickrateoptions.sampleroundingRight <= 0.1)
			return getColorFromFPS(60-(Math.abs((input - perfect) / perfect))*100);
		else
			return 0xffffff;
	}
	public int getAttackColorR(double input)
	{
		if (SuperClientConfig.display.clickrate.clickrateoptions.sampleroundingRight <= 0.1)
			return getColorFromFPS(input*6);
		else
			return 0xffffff;
	}
	public void drawStringAtPosition(int posX, int posY, int offX, int offY, String string, int color)
	{
		ScaledResolution r = new ScaledResolution(Minecraft.getMinecraft());
		int width = r.getScaledWidth();
		int height = r.getScaledHeight();
		int swidth = Minecraft.getMinecraft().fontRenderer.getStringWidth(string);
		int locX = 0;
		int locY = 0;

		if (posX == -1)
			locX = offX;
		else if (posX == 0)
			locX = offX + width / 2 - swidth / 2;
		else if (posX == 1)
			locX = offX + width - swidth;

		if (posY == -1)
			locY = offY;
		else if (posY == 0)
			locY = offY + height / 2 - 5;
		else if (posY == 1)
			locY = offY + height - 9;

		if (posY == -1 && posX == 0)
			locY += move;
		
		Minecraft.getMinecraft().fontRenderer.drawStringWithShadow(string, locX, locY, color);


	}
	
	public void drawTwoPartString(int posX, int posY, int offX, int offY, String string, int color, String string2, int color2)
	{
		ScaledResolution r = new ScaledResolution(Minecraft.getMinecraft());
		int width = r.getScaledWidth();
		int height = r.getScaledHeight();
		int s1width = Minecraft.getMinecraft().fontRenderer.getStringWidth(string);
		int s2width = Minecraft.getMinecraft().fontRenderer.getStringWidth(string2);
		int swidth = s1width + s2width;
		int locX = 0;
		int locY = 0;

		if (posX == -1)
			locX = offX;
		else if (posX == 0)
			locX = offX + width / 2 - swidth / 2;
		else if (posX == 1)
			locX = offX + width - swidth;

		if (posY == -1)
			locY = offY;
		else if (posY == 0)
			locY = offY + height / 2 - 5;
		else if (posY == 1)
			locY = offY + height - 9;

		if (posY == -1 && posX == 0)
			locY += move;

		Minecraft.getMinecraft().fontRenderer.drawStringWithShadow(string, locX, locY, color);
		Minecraft.getMinecraft().fontRenderer.drawStringWithShadow(string2, locX + s1width, locY, color2);

	}
}
