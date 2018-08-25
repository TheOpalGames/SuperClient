package com.aehmttw.clientmod;


import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.IOUtils;
import com.google.common.collect.Maps;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.EntityRenderer;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.entity.RenderPlayer;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.client.settings.GameSettings;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.ModMetadata;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.relauncher.ReflectionHelper;
import net.minecraftforge.fml.relauncher.ReflectionHelper.UnableToAccessFieldException;

@Mod(modid=SuperClientInfo.id, name="The Super Client", version=SuperClientInfo.version, acceptedMinecraftVersions="[1.12, 1.12.1, 1.12.2]", dependencies = "required-after:forge", clientSideOnly = true, canBeDeactivated=true)
public class SuperClient
{
	@Mod.Instance(value = "superclient")
	public static SuperClient instance;

	public static boolean toggle = SuperClientConfig.af.autofunctions;

	public static Configuration config;

	public static TextureManager t;
	public static boolean u;

	public static int[] charWidth;
	public static ModMetadata modMetadata;

	public static ArrayList<String> instructions = new ArrayList<String>();
	public static ArrayList<String[]> swaplist = new ArrayList<String[]>();
	
	static Minecraft mc = Minecraft.getMinecraft();
	
	static SuperClientEvent superclientevent;
	/**
	 * @param event
	 */
	@EventHandler
	public void preInit(FMLPreInitializationEvent event)
	{
		superclientevent = new SuperClientEvent();
		try
		{
			
			@SuppressWarnings("deprecation")
			List<String> list = IOUtils.readLines(new FileInputStream("superclient/versioninfo.yummypie"));
			for (String s: list)
			{
				if (s.startsWith("4"))
				{
					if (Integer.parseInt(s.split(">")[1]) < SuperClientInfo.config)
					{
						if (event.getSuggestedConfigurationFile().exists())
						{
							instructions.add("Your configuration file for The Super Client has been reset as it is out of date. Sorry!");
							event.getSuggestedConfigurationFile().delete();
						}
					}
				}
			}
		} catch (IOException e2) 
		{
			try 
			{
				new File("superclient/versioninfo.yummypie").createNewFile();
			} 
			catch (IOException e) 
			{
			}
		}
		config = new Configuration(event.getSuggestedConfigurationFile());

		if (!new File("superclient/versioninfo.yummypie").exists())
		{
			try {new File("superclient/versioninfo.yummypie").createNewFile();} catch (IOException e) {}
		}
		try
 	   	{
			PrintWriter printwriter = new PrintWriter(new OutputStreamWriter(new FileOutputStream("superclient/versioninfo.yummypie"), StandardCharsets.UTF_8));
	    	printwriter.println("This file stores version information to protect your configs and saved mod files (such as coordinates) from breaking with updates");
	    	printwriter.println("To prevent loss of saved coordinates and config options, don't change this file.");
			printwriter.println("1. Version build date ->"+SuperClientInfo.date);
	    	printwriter.println("2. Version number ->"+SuperClientInfo.version);
	    	printwriter.println("3. Version identifier ->"+SuperClientInfo.uid);
	    	printwriter.println("4. Configuration format ->"+SuperClientInfo.config);
	    	printwriter.println("5. Developer mode ->"+SuperClientInfo.cape_testing);

			printwriter.close();
 	   	}
 	   	catch (FileNotFoundException e)
 	   	{
 	   		try {new File("superclient/versioninfo.yummypie").createNewFile();} catch (IOException e1) {}
 	   	}

		toggle = SuperClientConfig.af.autofunctions;
		modMetadata = event.getModMetadata();
		modMetadata.modId		=	SuperClientInfo.id;
		modMetadata.name		=	"The Super Client";
		modMetadata.version		=	SuperClientInfo.version;
		modMetadata.url		=	"aehmttw.wordpress.com";
		modMetadata.authorList	=	Arrays.asList (new String[] { "aehmttw" });
		modMetadata.credits		=	"aehmttw";
		modMetadata.logoFile	        =	"/mclogo.png";

		new File("superclient/heads-up-display").mkdirs();
		SuperClientEvent.loadCoords();
		refreshDesc();
		wordSwap();

	}
	public static void refreshDesc()
	{
		String safety = "\u00A7cAutosneak and autosprint are disabled\n";
		if (toggle)
			safety = "\u00A7aAutosneak and autosprint are enabled (Please make sure these are allowed if playing multiplayer)!\n";

		String safety2 = "\u00A7cCustom camera is disabled\n";
		if (SuperClientConfig.cam.camera)
			safety2 = "\u00A7aCustom camera is enabled! (Please make sure this is allowed if playing multiplayer)! \u00A74\u00A7lThis will break compatibility with all OptiFine or GLSL Shaders Mod Shaders!\n";

		modMetadata.description	=
				"\u00A7c\u00A7lT\u00A76\u00A7lh\u00A7e\u00A7le \u00A7a\u00A7lS\u00A7b\u00A7lu\u00A7d\u00A7lp\u00A7c\u00A7le\u00A76\u00A7lr \u00A7e\u00A7lC\u00A7a\u00A7ll\u00A7b\u00A7li\u00A7d\u00A7le\u00A7c\u00A7ln\u00A76\u00A7lt\u00A7e\u00A7l!\n\u00A77by aehmttw\n\nScroll down to see the features of this mod!\n\n"
				+ safety + safety2 +
				 "\u00A7lTable of contents:\n"
				 	+ "1. What's new!\n"
				 	+ "2. The features of the mod\n"
				 	+ "3. A word on this mod's effects on anticheats\n"
				 	+ "4. A word on compatibility\n"
				 	+ "5. A word on mod file storage\n"
				 	+ "6. Brief changelog\n\n"
				+ "\u00A7lWhat's new:\nNew in v1.0 - Clickrate display, display edit menu, and more!\n\n"
				+ "\u00A7lWhat's included:"
				+ "\n\n\u00A7c6 in-game zooms:\n"
					+ SuperClientEvent.keyBindings[0].getDisplayName() + " - low\n"
					+ SuperClientEvent.keyBindings[1].getDisplayName() + " - medium-low\n"
					+ SuperClientEvent.keyBindings[2].getDisplayName() + " - medium\n"
					+ SuperClientEvent.keyBindings[3].getDisplayName() + " - medium-high\n"
					+ SuperClientEvent.keyBindings[4].getDisplayName() + " - high\n"
					+ SuperClientEvent.keyBindings[5].getDisplayName() + " - very high"
				+ "\n\n\u00A76Brightness Control:\n"
					+ SuperClientEvent.keyBindings[6].getDisplayName() + " - increase brightness\n"
					+ SuperClientEvent.keyBindings[7].getDisplayName() + " - decrease brightness\n"
					+ SuperClientEvent.keyBindings[8].getDisplayName() + " - full brightness\n"
					+ SuperClientEvent.keyBindings[9].getDisplayName() + " - well lit brightness\n"
					+ SuperClientEvent.keyBindings[10].getDisplayName() + " - bright brightness\n"
					+ SuperClientEvent.keyBindings[11].getDisplayName() + " - moody brightness\n\n"
				+ "\u00A7eIn-game info heads-up display:\n"
					+ "You can rearrange or disable these elements in the edit menu, accessed by pressing " + SuperClientEvent.keyBindings[23].getDisplayName()
					+ "\nEquipped Armor\n"
					+ "    \u00A77Armor name is colored based on material\n"
					+ "    \u00A77The color for each material can be customized in the config\n"
					+ "    \u00A77Armor durability is included and colored based on percentage left\n"
					+ "Held Items\n"
					+ "    \u00A77Item quantity is included\n"
					+ "    \u00A77Item durability is shown if applicable\n"
					+ "    \u00A77The text is colored based on durability percentage (if applicable)\n"
					+ "Active Status Effects\n"
					+ "    \u00A77These are listed in order of ID\n"
					+ "    \u00A77Effects are colored based on their type\n"
					+ "    \u00A77Duration is included and colored based on time left\n"
					+ "Motion Keybinds\n"
					+ "    \u00A77Keybinds for forward, back, left, right, and jump are shown\n"
					+ "    \u00A77They light up when pressed\n"
					+ "Hotbar Keybinds\n"
					+ "    \u00A77These are displayed above the hotbar\n"
					+ "    \u00A77The colors of the keybinds can be customized in the config\n"
					+ "Saved Coordinates\n"
					+ "    \u00A77The colors of the coordinates can be customized in the config\n"
					+ "    \u00A77More on saving coordinates later in the description\n"
					+ "Current Location\n"
					+ "World Spawn\n"
					+ "Username\n"
					+ "Framerate\n"
					+ "    \u00A77This is colored based on framerate\n"
					+ "Active Autofunctions\n"
					+ "    \u00A77Autosprint is displayed in cyan\n"
					+ "    \u00A77Autosneak is displayed in red"
					+ "Clickrate\n"
					+ "    \u00A77Optimal attack speed is displayed in parentheses\n"
					+ "    \u00A77Clickrate for both attack and use item is shown\n"
					+ "    \u00A77Clickrate rounding can be customized in the config\n"
					+ "    \u00A77Clickrate can either be based on the interval between clicks or the amount of clicks in a second\n"
					+ "    \u00A77Many options can be customized in the config\n"
					+ "    \u00A77Clickrate is colored based on accuracy, if rounded to the nearest tenth or less\n"
					+ "Custom text\n"
					+ "    \u00A77This is disabled by default"
				+ "\n\n\u00A7aAuto-functions: (enabled: " + toggle +")"
					+ "\n" + SuperClientEvent.keyBindings[21].getDisplayName() + " - toggle auto sprint"
					+ "\n" + SuperClientEvent.keyBindings[22].getDisplayName() + " - toggle auto sneak\n"
				+ "\n\u00A7bSave Coordinates:\n"
					+ "Press " + SuperClientEvent.keyBindings[12].getDisplayName() + " and a hotbar slot key (like 3) to save a coordinate to the slot of the hotbar key\n"
					+ "Press " + SuperClientEvent.keyBindings[13].getDisplayName() + " and a hotbar key to remove a stored coordinate from the slot of the hotbar key\n"
					+ "You can save up to 9 coordinates per profile\n"
					+ "You can change the active profile in the config (there is no limit to how many profiles you can have)!"
				+ "\n\n\u00A7dBetter rain and snow rendering:\n"
					+ "Precipitation draw distance on fast and fancy graphics is customizable, and rain or snow is randomly offset (to a customizable extent) to look more natural! (A custom rain texture is included and can be disabled in the config)\n"
				+ "\n\u00A7cCamera Manipulation:"
					+ "\n" + SuperClientEvent.keyBindings[14].getDisplayName() + " - Increase camera distance from player (3rd/2nd person view)"
					+ "\n" + SuperClientEvent.keyBindings[15].getDisplayName() + " - Decrease camera distance from player (3rd/2nd person view)"
					+ "\n" + SuperClientEvent.keyBindings[17].getDisplayName() + " - Lock the current camera angle"
					+ "\n" + SuperClientEvent.keyBindings[18].getDisplayName() + " - Look behind (player head will not move, just camera view)"
					+ "\n" + SuperClientEvent.keyBindings[16].getDisplayName() + " - Hold with a hotbar key to change camera view angle (will NOT move the player head)"
					+ "\n    " + mc.gameSettings.keyBindsHotbar[0] + " - Increase yaw (rotate around up-down axis)"
					+ "\n    " + mc.gameSettings.keyBindsHotbar[1] + " - Decrease yaw (rotate around up-down axis)"
					+ "\n    " + mc.gameSettings.keyBindsHotbar[2] + " - Increase pitch (rotate around left-right axis)"
					+ "\n    " + mc.gameSettings.keyBindsHotbar[3] + " - Decrease pitch (rotate around left-right axis)"
					+ "\n    " + mc.gameSettings.keyBindsHotbar[4] + " - Increase roll (rotate around forward-backward axis)"
					+ "\n    " + mc.gameSettings.keyBindsHotbar[5] + " - Decrease roll (rotate around forward-backward axis)"
					+ "\n    " + mc.gameSettings.keyBindsHotbar[6] + " - Reset yaw"
					+ "\n    " + mc.gameSettings.keyBindsHotbar[7] + " - Reset pitch"
					+ "\n    " + mc.gameSettings.keyBindsHotbar[8] + " - Reset roll\n"
				+ "\n\u00A77Bonus Features!:\n"
				+ "Hotbar slot and HUD armor name colors are customizable in the config!\n"
				+ "Render distance max value can be set in the config - Yes, this means you can use that 64-chunk render distance you have been dying to use!\n"
				+ "Friends list! - your friends will have a Friend tag above their name, press ; to see friends in view!.\n"
				+ "Capes for everyone who wants one! Contact me if you want a cape\n"
				+ "Load Shaders from resourcepacks! Yay!\n"
				+ "Font customization: better bold, font spacing, and shadow customization!\n\n"
				+ "\u00A7lAnti-Cheats:\nThis mod was developed with the aim of providing useful tools to the player, while not tripping server anti-cheats. Example: Unlike other mods that change player movement directly while autosneaking, this mod tricks the client into thinking the sneak key is pressed!\n\n"
				+ "\u00A7lCompatibility:\nThis mod overrides EntityRenderer (for rain and custom camera angles), FontRenderer (For better bolding on fonts thinner than vanilla's), and PlayerRenderer (to display capes and Friend tags). If any mod paired with it also plays with one of these classes, strange things may happen. Disable 'Enable Dynamic Rain/Snow' in 'dynamic rain and snow', 'Enable Camera Angle Changing' in 'camera', 'Enable Custom Font Renderer' in 'font', and 'Enable Custom Player Renderer' in 'player rendering' to ensure compatibility (You don't need to restart Minecraft for these to take effect!!!)."
				+ "\n\n\u00A7lMod file storage:"
				+ "\nAll files are stored in your minecraft directory."
				+ "\nThis mod stores a config file with all the config options in /config/superclient.cfg"
				+ "\nVersion information (which shouldn't be changed) is stored in /superclient/versioninfo.yummypie <- note the hilarious file extension :)"
				+ "\nSaved coordinates are stored in /superclient/coords/<profile name>/<coordinates index (0-8)>_coords.yummypie"
				+ "\nTutorial files are stores in /superclient/tutorial/<version>.yummypie - these are empty and used as markers, deleting them will show a tutorial again"
				+ "\nDisplay settings are stored in /superclient/heads-up-display/<display element>.yummypie"
				+ "\n\n\u00A7lChangelog:"
				+ "\nPlease note - this changelog is rather brief and doesn't include every small detail"
				+ "\n1.0 - Added edit mode and clickrate display"
				+ "\n0.9 - Added more font customizability options"
				+ "\n0.8 - Added shader support (for vanilla and resource pack shaders)"
				+ "\n0.7 - Added capes, friend list, and camera angles"
				+ "\n0.6 - Added hotbar and display armor name color customizability, and added coordinate saving"
				+ "\n0.5 - Added a configuration, move control display, custom string display, and hotbar numbers display"
				+ "\n0.4 - Added autofunctions (due to the lack of a config, 2 jar files - one with autofunctions and one without them - were released)"
				+ "\n0.3 - Added status effect display"
				+ "\n0.2 - Added armor and held item display, and FPS/Brightness display"
				+ "\n0.1 - Added 6 zooms, and 6 keys to control brightness"
				+ "\n\n\nYou've read it all! Have a Yummie Pie!";
	}

	public static void playerRenderer()
	{
		//if (Minecraft.getMinecraft().loadingScreen != null)
		//	Minecraft.getMinecraft().loadingScreen.displayLoadingString("Loading Player Renderer...");
		
    	Map<String, RenderPlayer> skinMap = Maps.<String, RenderPlayer>newHashMap();

		if (SuperClientConfig.player.customplayer)
		{
			RenderPlayer playerRenderer = new SuperClientPlayerRenderer(Minecraft.getMinecraft().getRenderManager());
			skinMap.put("default", playerRenderer);
        	skinMap.put("slim", new SuperClientPlayerRenderer(Minecraft.getMinecraft().getRenderManager(), true));
		}
		else
		{
			RenderPlayer playerRenderer = new RenderPlayer(Minecraft.getMinecraft().getRenderManager());
			skinMap.put("default", playerRenderer);
        	skinMap.put("slim", new RenderPlayer(Minecraft.getMinecraft().getRenderManager(), true));

		}
		try
        {
        	ReflectionHelper.setPrivateValue(RenderManager.class, Minecraft.getMinecraft().getRenderManager(), skinMap, "skinMap");
        }
        catch (UnableToAccessFieldException e)
        {
        	try
            {
            	ReflectionHelper.setPrivateValue(RenderManager.class, Minecraft.getMinecraft().getRenderManager(), skinMap, "field_178636_l");
            }
            catch (UnableToAccessFieldException e1)
            {
            	System.out.println("ouuuie... it seems like my render manager isn't working!");
            }
        }
	}
	public static void entityRenderer()
	{
		//if (Minecraft.getMinecraft().loadingScreen != null)
		//	Minecraft.getMinecraft().loadingScreen.displayLoadingString("Loading Camera...");
		
		if (SuperClientConfig.rs.dynamic_rain && SuperClientConfig.cam.camera)
			Minecraft.getMinecraft().entityRenderer = new SuperClientRain(Minecraft.getMinecraft(), Minecraft.getMinecraft().getResourceManager());
		else if (SuperClientConfig.rs.dynamic_rain)
			Minecraft.getMinecraft().entityRenderer = new SuperClientRain2(Minecraft.getMinecraft(), Minecraft.getMinecraft().getResourceManager());
		else if (SuperClientConfig.cam.camera)
			Minecraft.getMinecraft().entityRenderer = new SuperClientEntityRenderer(Minecraft.getMinecraft(), Minecraft.getMinecraft().getResourceManager());
		else
			Minecraft.getMinecraft().entityRenderer = new EntityRenderer(Minecraft.getMinecraft(), Minecraft.getMinecraft().getResourceManager());
	}
	public static void fontRenderer()
	{
		//if (Minecraft.getMinecraft().loadingScreen != null)
		//	Minecraft.getMinecraft().loadingScreen.displayLoadingString("Loading Font Renderer...");
		
		Minecraft m = Minecraft.getMinecraft();
		
		t = ReflectionHelper.getPrivateValue(FontRenderer.class, Minecraft.getMinecraft().fontRenderer, 7);
		u = ReflectionHelper.getPrivateValue(FontRenderer.class, Minecraft.getMinecraft().fontRenderer, 10);

		FontRenderer tm;
		if (SuperClientConfig.font.font && !(m.fontRenderer instanceof SuperClientFontRenderer))
			tm = new SuperClientFontRenderer();
		else if (!SuperClientConfig.font.font && m.fontRenderer instanceof SuperClientFontRenderer)
		{
			tm = new FontRenderer(Minecraft.getMinecraft().gameSettings, new ResourceLocation("textures/font/ascii.png"), SuperClient.t, SuperClient.u);
			try
			{
				ReflectionHelper.setPrivateValue(FontRenderer.class, tm, charWidth, "charWidth");
			}
			catch (Exception e)
			{
				System.out.println("Seriously? Optifine, just STOP MEDDLING with the font renderer. PLEASE!");
			}
		}
		else if (SuperClientConfig.font.font && m.fontRenderer instanceof SuperClientFontRenderer)
		{
			SuperClientFontRenderer font = (SuperClientFontRenderer)m.fontRenderer;
			font.loadColors();
			tm = m.fontRenderer;
		}
		else
			tm = m.fontRenderer;
		m.fontRenderer = tm;
    }
	@EventHandler
	public void load(FMLInitializationEvent event)
	{
		new File("superclient/tutorial").mkdirs();
		File tutorial = new File("superclient/tutorial/welcome.yummypie");
		if (!tutorial.exists())
		{
			instructions.add("Welcome to The Super Client!");
			instructions.add("This tutorial will now show you the features of this mod!");
			instructions.add("You can zoom your view by pressing V, B, N, M, Comma, or Period!");
			instructions.add("Each zoom has a different strength, V is the smallest, and Period is the largest.");
			instructions.add("You can also change your brightness ingame (Yes, this can mean fullbright!)");
			instructions.add("Press G, H, J, or K to use a preset brightness, or...");
			instructions.add("...increase your brightness with R, and decrease it with Z!");
			instructions.add("Your worn armor will be displayed in the top left corner, durability and all!");
			instructions.add("The colors used in the text displaying armor can be customized in the mod's config!");
			instructions.add("To access the config...");
			instructions.add("...press ESC, go to Mod Options, click The Super Client, and click Config.");
			instructions.add("Hover over config options' names to find out more about them!");
			instructions.add("The items you hold in your hand are displayed at the top of the screen!");
			instructions.add("If any held items have durability...");
			instructions.add("...they will be colored according to how much durability they have left!");
			instructions.add("Any active status effects are shown at the bottom left corner of the screen.");
			instructions.add("Your move controls are displayed at the bottom right, and light up when pressed!");
			instructions.add("Numbers above your hotbar are shown in customizable colors.");
			instructions.add("Your current coordinates are displayed in the bottom right corner...");
			instructions.add("Along with the world spawn!");
			instructions.add("You can also save up to 9 coordinates at a time...");
			instructions.add("...by pressing = and one of your hotbar slot keys at the same time!");
			instructions.add("You can delete coordinates as well...");
			instructions.add("...in the same fashion as adding them, but instead of holding =, hold -.");
			instructions.add("DON'T WORRY! You can save more than 9 coordinates in total...");
			instructions.add("...just change the folder they save to in the config!");
			instructions.add("If you can use /tp, you can teleport to saved coordinates...");
			instructions.add("...in the same fashion as adding them, but while holding 0!");
			instructions.add("A clickrate meter (CPS) is displayed when you click.");
			instructions.add("This meter changes colors depending on how well you time your clicks!");
			instructions.add("Your FPS and username are displayed at the top right.");
			instructions.add("You can also configure a custom text to be displayed at the top of your screen.");
			instructions.add("Press F12 to access display edit mode, to toggle or rearrange displays.");
			instructions.add("Done with the displays! Now for auto functions...");
			instructions.add("Press ` (Grave) (near the top left of your keyboard) to activate Auto Sprint.");
			instructions.add("When Auto Sprint is on, the game will think you are holding the sprint key!");
			instructions.add("Turn Auto Sprint off by pressing ` again.");
			instructions.add("Auto Sneak works in a similar fashion - its key is U.");
			instructions.add("These 2 functions can be considered cheating by servers...");
			instructions.add("...so it is best to turn them off to be safe.");
			instructions.add("You can also change your camera view without changing your player's orientation!");
			instructions.add("Press Left Alt to look behind... without moving your head...");
			instructions.add("It's just like you have eyes in the back of your head! LOL!");
			instructions.add("When you are in third person mode, you can...");
			instructions.add("...press I or O to change the camera's distance to the player!");
			instructions.add("You can also lock the camera's angle by pressing P...");
			instructions.add("...then you will be able to see your player's sides and back!");
			instructions.add("You can manually change the camera's angle by holding F10...");
			instructions.add("...and one of your hotbar slot keys.");
			instructions.add("These cool camera tricks can also be considered cheating...");
			instructions.add("...so you might want to turn them off on servers to be safe.");
			instructions.add("You can add friends in the Config...");
			instructions.add("...they will be tagged so you can easily recognize them!");
			instructions.add("When you press semicolon, the Friends in View display pops up!");
			instructions.add("That only works after you've completed the tutorial. (You're almost done!)");
			instructions.add("This mod also packs in some other graphical improvements:");
			instructions.add("Better rain and better HD font bolding");
			instructions.add("Don't forget to check out the config because nearly everything is changeable!");
			instructions.add("You can see this tutorial again...");
			instructions.add("...by deleting the tutorial folder in the superclient folder.");
			instructions.add("There is more information in the mod description, you can read that too!");
			instructions.add("Have fun!");

		}
		else
		{
			File v07 = new File("superclient/tutorial/0.7.0.yummypie");
			if (!v07.exists())
			{
				instructions.add("Welcome back! The Super Client has updated to 0.7!");
				instructions.add("Following are some new features of 0.7!");
				instructions.add("First, capes and custom elytras have been added! Snazzy! (LOL)");
				instructions.add("If you don't have a cape, don't feel sad! I will make one for you if you give me a design idea!");
				instructions.add("Next, there is a friend list in the Mod Options menu from the pause menu!");
				instructions.add("Adding a friend to this list makes a tag titled 'Friend!' appear above their name!");
				instructions.add("Finally, you can play with the camera with some new controls!");
				instructions.add("Press I or O in third person mode to change the camera's distance to the player!");
				instructions.add("Next, pressing Left Alt allows you to look behind... Without turning your head!");
				instructions.add("It's almost like you have eyes on the back of your head!!!");
				instructions.add("Press P to lock your camera angle. Press P to unlock it again.");
				instructions.add("Locking your camera angle allows you to see your player rotate!");
				instructions.add("Finally, you can press F10 along with a hotbar key to change the camera's angle");
				instructions.add("F10 + 1 or 2 rotate around the up-down axis, F10 + 7 resets this to normal.");
				instructions.add("F10 + 3 or 4 rotate around the left-right axis, F10 + 8 resets this");
				instructions.add("F10 + 5 or 6 rotate around the forward-backward axis, F10 + 9 resets this");
				instructions.add("That's all for now, enjoy!");
			}
			File v074 = new File("superclient/tutorial/0.7.4.yummypie");
			if (!v074.exists())
			{
				instructions.add("Welcome back! The Super Client has updated to 0.7.4!");
				instructions.add("Press semicolon to activate/deactivate the friends in view list!");
				instructions.add("Also, versions 0.7.1-0.7.4 fixed some bugs! Yay!");
				instructions.add("That's all for now, enjoy!");
			}
			File v08 = new File("superclient/tutorial/0.8.yummypie");
			if (!v08.exists())
			{
				instructions.add("Welcome back! The Super Client has updated to 0.8!");
				instructions.add("Following are some new features of 0.8!");
				instructions.add("In the config, you can change the active shader!");
				instructions.add("Also, some bugs were fixed! Yay!");
				instructions.add("That's all for now, enjoy!");
			}
			File v09 = new File("superclient/tutorial/0.9.yummypie");
			if (!v09.exists())
			{
				instructions.add("Welcome back! The Super Client has updated to 0.9!");
				instructions.add("Following are some new features of 0.9!");
				instructions.add("You can now configure font shadows and colors!");
				instructions.add("The world spawn point is displayed in the bottom right!");
				instructions.add("(This can be disabled in the config)!");
				instructions.add("Also, many improvements were made! Yay!");
				instructions.add("That's all for now, enjoy!");
			}
			File v10 = new File("superclient/tutorial/1.0.yummypie");
			if (!v10.exists())
			{
				instructions.add("Welcome back! The Super Client has updated to 1.0!");
				instructions.add("Following are some new features of 1.0!");
				instructions.add("An edit menu for displays was added, accessed by pressing F12!");
				instructions.add("Added clickrate meters!");
				instructions.add("You can now teleport to saved coordinates if you have the permission...");
				instructions.add("By holding 0 and the saved coordinate slot hotbar key!");
				instructions.add("You might have the perspective key bound to 0 as well...");
				instructions.add("...the default perspective keybind is now F10!");
				instructions.add("Be sure to fix your keybind for perspective!");
				instructions.add("Also, many improvements were made! Yay! (And some easter eggs were added!)");
				instructions.add("That's all for now, enjoy!");
			}
			//note to self - when adding new version info, remember to create file in SC event
		}
		instructions.add("The Super Client!");
	}
	
	public static void wordSwap()
	{
		SuperClient.swaplist.clear();
		for (int i = 0; i < SuperClientConfig.font.swaplist.length; i++)
		{
			try
			{
				String s1 = SuperClientConfig.font.swaplist[i].split(" -> ")[0];
				String s2 = SuperClientConfig.font.swaplist[i].split(" -> ")[1];
				s1.replace("&&", "\u00A7");
				s2.replace("&&", "\u00A7");
				SuperClient.swaplist.add(new String[]{s1, s2});
			}
			catch (Exception e) {}
		}
	}
	
	@EventHandler
	public void postInit(FMLPostInitializationEvent event)
	{
		GameSettings.Options.RENDER_DISTANCE.setValueMax(SuperClientConfig.max_render_distance);
		GameSettings.Options.GAMMA.setValueMax(15.0F);
		playerRenderer();
		fontRenderer();
		entityRenderer();

		MinecraftForge.EVENT_BUS.register(superclientevent);
	}
	
}
