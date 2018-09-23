package net.theopalgames.superclient;


import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.GameSettings;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.config.Config;
import net.minecraftforge.common.config.ConfigManager;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.fml.client.event.ConfigChangedEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.ReflectionHelper;
//notice - configuration code was adapted from choonster's testmod3

import java.io.File;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Field;
import java.util.Map;
import java.util.Optional;



@Config(modid = "superclient")
public class SuperClientConfig
{
	@Config.Name("Auto Functions Settings")
	public static AutoFunctions af = new AutoFunctions();

	public static class AutoFunctions
	{

		@Config.Comment("Autosneak and Autosprint")
		@Config.Name("Enable Auto Functions")
		public boolean autofunctions = true;

	}

	@Config.Name("Player Settings")
	public static PlayerRendering player = new PlayerRendering();

	public static class PlayerRendering
	{
		@Config.Name("Enable Custom Player Renderer")
		@Config.Comment("Allows for capes and friend tags! Don't turn this off unless you need compatibility with other mods that modify the PlayerRenderer!")
		public boolean customplayer = true;

	}

	@Config.Name("Camera Settings")
	public static Camera cam = new Camera();

	public static class Camera
	{
		@Config.Comment("Allows for custom fogs and camera angles. WILL BREAK COMPATIBILITY WITH ALL SHADERS FROM OPTIFINE / GLSL SHADERS MOD!")
		@Config.Name("Enable Custom Camera")
		public  boolean camera = true;
		@Config.Comment("Distance of player from camera in third person view by default.")
		@Config.Name("Default Third Person Distance")
		public  float thirdPersonDistance = 4;
		@Config.Name("Blindness Fog Distance")
		public  double blindness = 5;
	}

	@Config.Name("Font Settings")
	public static Font font = new Font();
	public static class Font
	{
		@Config.Name("Enable Custom Font Renderer")
		@Config.Comment("Allows for better font bolding, custom font colors and shadows, and custom font spacing, but may break compatibility with other mods.")
		public  boolean font = true;

		@Config.Name("Enable Better Thin Font Bolding")
		@Config.Comment("Instead of displaying one 2 characters one Minecraft font pixel apart, multiple characters are displayed on top of each other, closer together.")
		public  boolean boldfont = true;

		@Config.Name("Bold Font Thickness")
		@Config.RangeDouble(min=0,max=2)
		public double thickness = 1;

		@Config.Name("Bold Font Frequency")
		@Config.Comment("This should be set to 1 divided by the thinnest horizontal thickness of the font you use. A font with 1/4 of a Minecraft font pixel thickness should use 4")
		@Config.RangeInt(min=1)
		public int frequency = 4;
		
		@Config.Name("Text Shadow Frequency")
		@Config.Comment("Larger numbers result in a 3-dimensional feel to the font, no need to set it too high as it will cause lag without any visual difference. Use 1 for vanilla font shadows.")
		@Config.RangeInt(min=1)
		public int shadowfrequency = 8;
		
		@Config.Name("Text Shadow Length")
		@Config.Comment("Length of the shadows. Larger values mean longer shadows!")
		@Config.RangeDouble(min=0,max=10)
		public double shadowlength = 1;

		@Config.Name("Font Spacing")
		@Config.RangeDouble(min=0,max=3)
		public double spacing = 1;
		
		@Config.Name("Font Shadow Color Multiplier")
		@Config.Comment("How much to multiply the colors of font shadows by. Disabled when using custom shadow colors.")
		@Config.RangeDouble(min=0)
		public double shadowmultiplier = 0.25;
		
		@Config.Name("Font Shadow X Offset")
		@Config.Comment("Controls the X direction of the shadow")
		@Config.RangeDouble(min=-1,max=1)
		public double shadowX = 1;
		@Config.Name("Font Shadow Y Offset")
		@Config.Comment("Controls the Y direction of the shadow")
		@Config.RangeDouble(min=-1,max=1)
		public double shadowY = 1;
		
		@Config.Comment("Put a -> with spaces before and after it between phrases to replace. This is only a visual effect.")
		@Config.Name("Phrase replace list")
		public String[] swaplist = new String[0];
		
		@Config.Name("Color Settings")
		public ColorCodes colorcodes = new ColorCodes();

		@Config.Name("other settings")
		public OtherSettings other = new OtherSettings();
		public static class OtherSettings
		{
			//@Config.Name("Enable Glint")
			//@Config.Comment("Text will 'shine'!")
			//public boolean glint = false;
			
			@Config.Name("Enable Awesomeness")
			@Config.Comment("Increases awesomeness by 256%")
			public boolean rainbowmode = false;
			@Config.Name("Enable Ultimate Awesomeness")
			@Config.Comment("Increases awesomeness even more by 512%")
			public boolean animatedrainbowmode = false;
			
			@Config.Name("Ultimate Awesomeness Frequency")
			public double rainbowsize = 1;
			@Config.Name("Awesomeness Speed")
			public double rainbowspeed = 0.33;
			
			@Config.Name("Nonify aehmttw")
			@Config.Comment("This option does nothing... At least, nothing useful for you...")
			public boolean nonify = true;
		}

		public static class ColorCodes
		{
			@Config.Name("Custom Color Code Colors")
			@Config.Comment("Enable if you want to change the colors of the default tints Minecraft applies to text that will be colored.")
			public boolean customcolor = false;
			@Config.Name("Custom Color Shadow Colors")
			@Config.Comment("Enable if you want to change the colors of the default tints Minecraft applies to shadows of text that will be colored.")
			public boolean shadowcolor = false;
			
			@Config.Name("Use RRRGGGBBB Text Colors")
			@Config.Comment("Instead of Red*256*256 + Green*256 + Blue, use the first 3 digits as red, the next 3 as green, and the last 3 as blue (max: 255)")
			public  boolean use_RRRGGGBBB_text = false;
			
			@Config.Name("Color 0 (\u00A700\u00A77)")
			public int color0 = 0;
			@Config.Name("Color 1 (\u00A711\u00A77)")
			public int color1 = 0x0000ff;
			@Config.Name("Color 2 (\u00A722\u00A77)")
			public int color2 = 0x009900;
			@Config.Name("Color 3 (\u00A733\u00A77)")
			public int color3 = 0x009999;
			@Config.Name("Color 4 (\u00A744\u00A77)")
			public int color4 = 0x990000;
			@Config.Name("Color 5 (\u00A755\u00A77)")
			public int color5 = 0x8800ff;
			@Config.Name("Color 6 (\u00A766\u00A77)")
			public int color6 = 0xffbb00;
			@Config.Name("Color 7 (\u00A777\u00A77)")
			public int color7 = 0x999999;
			@Config.Name("Color 8 (\u00A788\u00A77)")
			public int color8 = 0x7f7f7f;
			@Config.Name("Color 9 (\u00A799\u00A77)")
			public int color9 = 0x007fff;
			@Config.Name("Color a (\u00A7aa\u00A77)")
			public int color10 = 0x00ff00;
			@Config.Name("Color b (\u00A7bb\u00A77)")
			public int color11 = 0x00ffff;
			@Config.Name("Color c (\u00A7cc\u00A77)")
			public int color12 = 0xff0000;
			@Config.Name("Color d (\u00A7dd\u00A77)")
			public int color13 = 0xff00ff;
			@Config.Name("Color e (\u00A7ee\u00A77)")
			public int color14 = 0xffff00;
			@Config.Name("Color f (\u00A7ff\u00A77)")
			public int color15 = 0xffffff;
			
			@Config.Name("Color 0 (\u00A700\u00A77) Shadow")
			public int color0s = 0;
			@Config.Name("Color 1 (\u00A711\u00A77) Shadow")
			public int color1s = 0x00007f;
			@Config.Name("Color 2 (\u00A722\u00A77) Shadow")
			public int color2s = 0x004900;
			@Config.Name("Color 3 (\u00A733\u00A77) Shadow")
			public int color3s = 0x004949;
			@Config.Name("Color 4 (\u00A744\u00A77) Shadow")
			public int color4s = 0x490000;
			@Config.Name("Color 5 (\u00A755\u00A77) Shadow")
			public int color5s = 0x4400ff;
			@Config.Name("Color 6 (\u00A766\u00A77) Shadow")
			public int color6s = 0x7f6600;
			@Config.Name("Color 7 (\u00A777\u00A77) Shadow")
			public int color7s = 0x494949;
			@Config.Name("Color 8 (\u00A788\u00A77) Shadow")
			public int color8s = 0x333333;
			@Config.Name("Color 9 (\u00A799\u00A77) Shadow")
			public int color9s = 0x00397f;
			@Config.Name("Color a (\u00A7aa\u00A77) Shadow")
			public int color10s = 0x007f00;
			@Config.Name("Color b (\u00A7bb\u00A77) Shadow")
			public int color11s = 0x007f7f;
			@Config.Name("Color c (\u00A7cc\u00A77) Shadow")
			public int color12s = 0x7f0000;
			@Config.Name("Color d (\u00A7dd\u00A77) Shadow")
			public int color13s = 0x7f007f;
			@Config.Name("Color e (\u00A7ee\u00A77) Shadow")
			public int color14s = 0x7f7f00;
			@Config.Name("Color f (\u00A7ff\u00A77) Shadow")
			public int color15s = 0x7f7f7f;
		}
	}


	@Config.Name("Heads-up-display Settings")
	public static Display display = new Display();

	public static class Display
	{
		@Config.Name("armor")
		public Armor armor = new Armor();
		public static class Armor
		{
			/*@Config.Name("position")
			public Position position = new Position();
			public static class Position
			{
				@Config.Name("Include on Screen")
				public boolean show = true;
				
				@Config.Comment("Set to -1 to align to left, 0 for middle, and 1 for right")
				@Config.Name("X Position")
				@Config.RangeInt(min=-1,max=1)
				public int locX = -1; 
				@Config.Comment("Set to -1 to align to top, 0 for middle, and 1 for bottom")
				@Config.Name("Y Position")
				@Config.RangeInt(min=-1,max=1)
				public int locY = -1; 
				@Config.Name("X Offset")
				public int offX = 1; 
				@Config.Name("Y Offset")
				public int offY = 1; 
				
			}*/
			@Config.Name("material colors")
			public ArmorColors colors = new ArmorColors();
			public static class ArmorColors
			{
				@Config.Name("Diamond")
				public  int armor_color_diamond = 0x33ebcb;
				@Config.Name("Iron")
				public  int armor_color_iron = 0xc6c6c6;
				@Config.Name("Chain")
				public  int armor_color_chainmail = 0x6d6d6d;
				@Config.Name("Leather")
				public  int armor_color_leather = 0x82583e;
				@Config.Name("Gold")
				public  int armor_color_gold = 0xeaee57;
				@Config.Name("Elytra")
				public  int armor_color_elytra = 0xd2b4dd;
				@Config.Name("Pumpkin")
				public  int armor_color_pumpkin = 0xe3901d;
				@Config.Name("Other")
				public  int armor_color_default = 0xffffff;
				@Config.Name("Use RRRGGGBBB Armor Colors")
				@Config.Comment("Instead of Red*256*256 + Green*256 + Blue, use the first 3 digits as red, the next 3 as green, and the last 3 as blue (max: 255)")
				public boolean use_RRRGGGBBB_armor = false;
			}
		}
		/*@Config.Name("held items")
		public HeldItems helditems = new HeldItems();
		public static class HeldItems
		{
			@Config.Name("position")
			public Position position = new Position();
			public static class Position
			{
				@Config.Name("Include on Screen")
				public boolean show = true;
				
				@Config.Comment("Set to -1 to align to left, 0 for middle, and 1 for right")
				@Config.Name("X Position")
				@Config.RangeInt(min=-1,max=1)
				public int locX = 0; 
				@Config.Comment("Set to -1 to align to top, 0 for middle, and 1 for bottom")
				@Config.Name("Y Position")
				@Config.RangeInt(min=-1,max=1)
				public int locY = -1; 
				@Config.Name("X Offset")
				public int offX = 0; 
				@Config.Name("Y Offset")
				public int offY = 1; 
				
			}
		}*/
		@Config.Name("custom text")
		public CustomText text = new CustomText();
		public static class CustomText
		{
			/*@Config.Name("position")
			public Position position = new Position();
			public static class Position
			{
				@Config.Name("Include on Screen")
				public boolean show = false;
				
				@Config.Comment("Set to -1 to align to left, 0 for middle, and 1 for right")
				@Config.Name("X Position")
				@Config.RangeInt(min=-1,max=1)
				public int locX = 0; 
				@Config.Comment("Set to -1 to align to top, 0 for middle, and 1 for bottom")
				@Config.Name("Y Position")
				@Config.RangeInt(min=-1,max=1)
				public int locY = -1; 
				@Config.Name("X Offset")
				public int offX = 0; 
				@Config.Name("Y Offset")
				public int offY = 1; 
				
			}*/
			
			@Config.Name("text")
			public Text text = new Text();
			public static class Text
			{
				@Config.Name("Text")
				public String custom_string = SuperClientInfo.uid;
				@Config.Name("Color")
				public int custom_string_color = 0xabcdef;
			}
			
		}
		/*@Config.Name("username")
		public Username username = new Username();
		public static class Username
		{
			@Config.Name("position")
			public Position position = new Position();
			public static class Position
			{
				@Config.Name("Include on Screen")
				public boolean show = true;
				
				@Config.Comment("Set to -1 to align to left, 0 for middle, and 1 for right")
				@Config.Name("X Position")
				@Config.RangeInt(min=-1,max=1)
				public int locX = 1; 
				@Config.Comment("Set to -1 to align to top, 0 for middle, and 1 for bottom")
				@Config.Name("Y Position")
				@Config.RangeInt(min=-1,max=1)
				public int locY = -1; 
				@Config.Name("X Offset")
				public int offX = -1; 
				@Config.Name("Y Offset")
				public int offY = 1; 
				
			}
		}
		@Config.Name("framerate")
		public FPS fps = new FPS();
		public static class FPS
		{
			@Config.Name("position")
			public Position position = new Position();
			public static class Position
			{
				@Config.Name("Include on Screen")
				public boolean show = true;
				
				@Config.Comment("Set to -1 to align to left, 0 for middle, and 1 for right")
				@Config.Name("X Position")
				@Config.RangeInt(min=-1,max=1)
				public int locX = 1; 
				@Config.Comment("Set to -1 to align to top, 0 for middle, and 1 for bottom")
				@Config.Name("Y Position")
				@Config.RangeInt(min=-1,max=1)
				public int locY = -1; 
				@Config.Name("X Offset")
				public int offX = -1; 
				@Config.Name("Y Offset")
				public int offY = 10; 
				
			}
		}
		@Config.Name("autofunctions")
		public AutoFunctions autofunctions = new AutoFunctions();
		public static class AutoFunctions
		{
			@Config.Name("position")
			public Position position = new Position();
			public static class Position
			{
				@Config.Name("Include on Screen")
				public boolean show = true;
				
				@Config.Comment("Set to -1 to align to left, 0 for middle, and 1 for right")
				@Config.Name("X Position")
				@Config.RangeInt(min=-1,max=1)
				public int locX = 1; 
				@Config.Comment("Set to -1 to align to top, 0 for middle, and 1 for bottom")
				@Config.Name("Y Position")
				@Config.RangeInt(min=-1,max=1)
				public int locY = -1; 
				@Config.Name("X Offset")
				public int offX = -1; 
				@Config.Name("Y Offset")
				public int offY = 19; 
				
			}
		}
		@Config.Name("status effects")
		public StatusEffects statuseffects = new StatusEffects();
		public static class StatusEffects
		{
			@Config.Name("position")
			public Position position = new Position();
			public static class Position
			{
				@Config.Name("Include on Screen")
				public boolean show = true;
				
				@Config.Comment("Set to -1 to align to left, 0 for middle, and 1 for right")
				@Config.Name("X Position")
				@Config.RangeInt(min=-1,max=1)
				public int locX = -1; 
				@Config.Comment("Set to -1 to align to top, 0 for middle, and 1 for bottom")
				@Config.Name("Y Position")
				@Config.RangeInt(min=-1,max=1)
				public int locY = 1; 
				@Config.Name("X Offset")
				public int offX = 1; 
				@Config.Name("Y Offset")
				public int offY = -1; 
				
			}
		}*/
		@Config.Name("hotbar keybinds")
		public HotbarNumbers hotbarnumbers = new HotbarNumbers();
		public static class HotbarNumbers
		{
			/*@Config.Name("position")
			public Position position = new Position();
			public static class Position
			{
				@Config.Name("Include on Screen")
				public boolean show = true;
				
				@Config.Name("X Offset")
				public int offX = 0; 
				@Config.Name("Y Offset")
				public int offY = 0; 
			}*/
			
			@Config.Name("keybind colors")
			public Hotbar colors = new Hotbar();
			public static class Hotbar
			{
				@Config.Comment("Colors for hotbar numbers with gradients supported if less than 9 colors are given (Red * 256 * 256 + Green * 256 + Blue): Black - 0, Gray - 8355711, White - 16777215, Red - 16711680, Orange - 16744192, Yellow - 16776960, Green - 65280, Cyan - 65535, Blue - 255, Purple - 8323327, Magenta - 16711935")
				@Config.Name("Keybind Colors")
				public  int[] hotbar_colors = new int[]{0xff7f7f, 0xffaa7f, 0xffff7f, 0x7fff7f, 0x7fffff, 0x7faaff, 0x7f7fff, 0xaa7fff, 0xff7fff};
				@Config.Name("Use RRRGGGBBB Colors")
				@Config.Comment("Instead of Red*256*256 + Green*256 + Blue, use the first 3 digits as red, the next 3 as green, and the last 3 as blue (max: 255)")
				public  boolean use_RRRGGGBBB = false;
			}
		}
		/*@Config.Name("move controls")
		public MoveControls movecontrols = new MoveControls();
		public static class MoveControls
		{
			@Config.Name("position")
			public Position position = new Position();
			public static class Position
			{
				@Config.Name("Include on Screen")
				public boolean show = true;
				
				@Config.Comment("Set to -1 to align to left, 0 for middle, and 1 for right")
				@Config.Name("X Position")
				@Config.RangeInt(min=-1,max=1)
				public int locX = 1; 
				@Config.Comment("Set to -1 to align to top, 0 for middle, and 1 for bottom")
				@Config.Name("Y Position")
				@Config.RangeInt(min=-1,max=1)
				public int locY = 1; 
				@Config.Name("X Offset")
				public int offX = -12; 
				@Config.Name("Y Offset")
				public int offY = -30; 
				
			}
		}
		@Config.Name("location")
		public Location location = new Location();
		public static class Location
		{
			@Config.Name("position")
			public Position position = new Position();
			public static class Position
			{
				@Config.Name("Include on Screen")
				public boolean show = true;
				
				@Config.Comment("Set to -1 to align to left, 0 for middle, and 1 for right")
				@Config.Name("X Position")
				@Config.RangeInt(min=-1,max=1)
				public int locX = 1; 
				@Config.Comment("Set to -1 to align to top, 0 for middle, and 1 for bottom")
				@Config.Name("Y Position")
				@Config.RangeInt(min=-1,max=1)
				public int locY = 1; 
				@Config.Name("X Offset")
				public int offX = -1; 
				@Config.Name("Y Offset")
				public int offY = -1; 
				
				public int[] getPosition()
				{
					return new int[]{locX, locY, offX, offY};
				}
			}
		}
		@Config.Name("world spawn")
		public Spawn spawn = new Spawn();
		public static class Spawn
		{
			@Config.Name("position")
			public Position position = new Position();
			public static class Position
			{
				@Config.Name("Include on Screen")
				public boolean show = true;
				
				@Config.Comment("Set to -1 to align to left, 0 for middle, and 1 for right")
				@Config.Name("X Position")
				@Config.RangeInt(min=-1,max=1)
				public int locX = 1; 
				@Config.Comment("Set to -1 to align to top, 0 for middle, and 1 for bottom")
				@Config.Name("Y Position")
				@Config.RangeInt(min=-1,max=1)
				public int locY = 1; 
				@Config.Name("X Offset")
				public int offX = -1; 
				@Config.Name("Y Offset")
				public int offY = -10; 
				
				public int[] getPosition()
				{
					return new int[]{locX, locY, offX, offY};
				}
			}
		}*/
		@Config.Name("clickrate")
		public Clickrate clickrate = new Clickrate();
		public static class Clickrate
		{
			
			/*@Config.Name("attack - position")
			public PositionL positionL = new PositionL();
			public static class PositionL
			{
				@Config.Name("Include on Screen")
				public boolean show = true;
				
				@Config.Comment("Set to -1 to align to left, 0 for middle, and 1 for right")
				@Config.Name("X Position")
				@Config.RangeInt(min=-1,max=1)
				public int locX = 1; 
				@Config.Comment("Set to -1 to align to top, 0 for middle, and 1 for bottom")
				@Config.Name("Y Position")
				@Config.RangeInt(min=-1,max=1)
				public int locY = -1; 
				@Config.Name("X Offset")
				public int offX = -1; 
				@Config.Name("Y Offset")
				public int offY = 31; 
				
				public int[] getPosition()
				{
					return new int[]{locX, locY, offX, offY};
				}
			}
			@Config.Name("use - position")
			public PositionR positionR = new PositionR();
			public static class PositionR
			{
				@Config.Name("Include on Screen")
				public boolean show = true;
				
				@Config.Comment("Set to -1 to align to left, 0 for middle, and 1 for right")
				@Config.Name("X Position")
				@Config.RangeInt(min=-1,max=1)
				public int locX = 1; 
				@Config.Comment("Set to -1 to align to top, 0 for middle, and 1 for bottom")
				@Config.Name("Y Position")
				@Config.RangeInt(min=-1,max=1)
				public int locY = -1; 
				@Config.Name("X Offset")
				public int offX = -1; 
				@Config.Name("Y Offset")
				public int offY = 41;
			}*/
			
			@Config.Name("clickrate options")
			public ClickrateOptions clickrateoptions = new ClickrateOptions();
			public static class ClickrateOptions
			{
				@Config.Name("Clickrate sample time (ms)")
				public int sampletime = 1000;
				@Config.Name("Clickrate rounding (attack)")
				public double sampleroundingLeft = 0.1;
				@Config.Name("Clickrate rounding (use)")
				public double sampleroundingRight = 1;
				@Config.Comment("Instead of dividing the amount of clicks in a specific time interval by that time interval, use the time between your last 2 clicks. More suitable for combat with attack cooldown.")
				@Config.Name("Use click interval (attack)")
				public boolean intervalLeft = true;
				@Config.Comment("Instead of dividing the amount of clicks in a specific time interval by that time interval, use the time between your last 2 clicks. More suitable for combat with attack cooldown.")
				@Config.Name("Use click interval (use)")
				public boolean intervalRight = false;
				@Config.Name("Stabilize clickrate digits")
				@Config.Comment("This option will add an extra 0 in front of the clickrate number if it is in the single digits")
				public boolean stabilize = false;
				@Config.Name("Show attack speed")
				public boolean optimal = true;
				@Config.Comment("Example: instead of Left Click, show *1")
				@Config.Name("Keybind shorthand mode")
				public boolean shorthand = false;
			}
		}
		@Config.Name("saved coordinates")
		public Coordinates coords = new Coordinates();
		public static class Coordinates
		{
			/*@Config.Name("position")
			public Position position = new Position();
			public static class Position
			{
				@Config.Name("Include on Screen")
				public boolean show = true;
				
				@Config.Comment("Set to -1 to align to left, 0 for middle, and 1 for right")
				@Config.Name("X Position")
				@Config.RangeInt(min=-1,max=1)
				public int locX = -1; 
				@Config.Comment("Set to -1 to align to top, 0 for middle, and 1 for bottom")
				@Config.Name("Y Position")
				@Config.RangeInt(min=-1,max=1)
				public int locY = 0; 
				@Config.Name("X Offset")
				public int offX = 1; 
				@Config.Name("Y Offset")
				public int offY = 0; 
	
				public int[] getPosition()
				{
					return new int[]{locX, locY, offX, offY};
				}
			}*/
				@Config.Name("coordinate colors")
				public Colors colors = new Colors();
				public static class Colors
				{
					@Config.Comment("Colors for coordinates with gradients supported if less than 9 colors are given (Red * 256 * 256 + Green * 256 + Blue): Black - 0, Gray - 8355711, White - 16777215, Red - 16711680, Orange - 16744192, Yellow - 16776960, Green - 65280, Cyan - 65535, Blue - 255, Purple - 8323327, Magenta - 16711935")
					@Config.Name("Coordinate Colors")
					public  int[] coord_colors = new int[]{0xff7f7f, 0xffaa7f, 0xffff7f, 0x7fff7f, 0x7fffff, 0x7faaff, 0x7f7fff, 0xaa7fff, 0xff7fff};
					@Config.Name("Use RRRGGGBBB Colors")
					@Config.Comment("Instead of Red*256*256 + Green*256 + Blue, use the first 3 digits as red, the next 3 as green, and the last 3 as blue (max: 255)")
					public  boolean use_RRRGGGBBB = false;
				}
				@Config.Name("Saved Coordinate Folder")
				public String saved_coordinate_profile = "default";
		}
		
		
		
		//	Legacy configuration
		/*@Config.Name("Clickrate sample time (ms)")
		public int sampletime = 1000;
		@Config.Name("Clickrate rounding")
		public double samplerounding = 1;
		@Config.Comment("Instead of dividing the amount of clicks in a specific time interval by that time interval, use the time between your last 2 clicks. More suitable for combat with attack cooldown.")
		@Config.Name("Use click interval")
		public boolean interval = false;
		
		@Config.Name("Default Display Options")
		public Defaults def = new Defaults();
		public static class Defaults
		{
			@Config.Name("Display Item Info By Default")
			public  boolean enable_item_info_by_default = true;
			@Config.Name("Display Status Effect Info By Default")
			public  boolean enable_status_effect_info_by_default = true;
			@Config.Name("Display Hotbar Slot Keybinds By Default")
			public  boolean enable_hotbar_numbers_by_default = true;
			@Config.Name("Display Extra Info By Default")
			public  boolean enable_extra_info_by_default = true;
			@Config.Name("Display Move Controls By Default")
			public  boolean enable_move_pad_by_default = true;
			@Config.Name("Display Coordinates By Default")
			public  boolean enable_coordinates_by_default = true;
		}

		@Config.Name("Custom Title Settings")
		public CustomString title = new CustomString();

		public static class CustomString
		{
			@Config.Name("Display Custom String")
			public  boolean show_custom_string = false;
			@Config.Name("Custom String")
			public  String custom_string = SuperClientInfo.uid;
			@Config.Name("Custom String Color")
			public  int custom_string_color = 0xabcdef;
		}

		@Config.Name("Armor Display Color Settings")
		public ArmorColors armorlegacy = new ArmorColors();
		public static class ArmorColors
		{
			@Config.Name("Display Armor Color - Diamond")
			public  int armor_color_diamond = 0x33ebcb;
			@Config.Name("Display Armor Color - Iron")
			public  int armor_color_iron = 0xc6c6c6;
			@Config.Name("Display Armor Color - Chain")
			public  int armor_color_chainmail = 0x6d6d6d;
			@Config.Name("Display Armor Color - Leather")
			public  int armor_color_leather = 0x82583e;
			@Config.Name("Display Armor Color - Gold")
			public  int armor_color_gold = 0xeaee57;
			@Config.Name("Display Worn Item Color - Elytra")
			public  int armor_color_elytra = 0xd2b4dd;
			@Config.Name("Display Worn Item Color - Pumpkin")
			public  int armor_color_pumpkin = 0xe3901d;
			@Config.Name("Display Worn Item Color - Other")
			public  int armor_color_default = 0xffffff;
			@Config.Name("Use RRRGGGBBB Armor Colors")
			@Config.Comment("Instead of Red*256*256 + Green*256 + Blue, use the first 3 digits as red, the next 3 as green, and the last 3 as blue (max: 255)")
			public boolean use_RRRGGGBBB_armor = false;

		}
		@Config.Name("Hotbar Number Display Color Settings")
		public Hotbar hotbar = new Hotbar();
		public static class Hotbar
		{
			@Config.Comment("Colors for hotbar numbers with gradients supported if less than 9 colors are given (Red * 256 * 256 + Green * 256 + Blue): Black - 0, Gray - 8355711, White - 16777215, Red - 16711680, Orange - 16744192, Yellow - 16776960, Green - 65280, Cyan - 65535, Blue - 255, Purple - 8323327, Magenta - 16711935")
			@Config.Name("Hotbar Keybind Colors")
			public  int[] hotbar_colors = new int[]{0xff7f7f, 0xffaa7f, 0xffff7f, 0x7fff7f, 0x7fffff, 0x7faaff, 0x7f7fff, 0xaa7fff, 0xff7fff};
			@Config.Name("Use RRRGGGBBB Hotbar Colors")
			@Config.Comment("Instead of Red*256*256 + Green*256 + Blue, use the first 3 digits as red, the next 3 as green, and the last 3 as blue (max: 255)")
			public  boolean use_RRRGGGBBB_hotbar = false;
		}*/
	}
	/*@Config.Name("Saved Coordinate Settings")
	public static Coordinates coords = new Coordinates();
	public static class Coordinates
	{
		@Config.Name("Saved Coordinate Folder")
		public String saved_coordinate_profile = "default";
		@Config.Name("Show World Spawn")
		public boolean showspawn = true;
	}*/

	@Config.Name("Dynamic Rain and Snow Settings")
	public static RainSnow rs = new RainSnow();

	public static class RainSnow
	{
		@Config.Name("Enable Dynamic Rain/Snow")
		@Config.Comment("Allows for the use of custom rain draw distances (thicknesses), and allows you to use the different rain texture (one with smaller droplets and clearer raindrops). May break compatibility with other mods that override EntityRenderer.")
		public  boolean dynamic_rain = true;
		@Config.Name("Dynamic Rain/Snow Randomness")
		@Config.Comment("How much you want to randomly offset \"Sheets\" of rain from each block. Low values have less randomness and can create weird patterns in rain or show with a large draw distance.")
		@Config.RangeDouble(min=0,max=10)
		public  double rain_offset = 3;
		@Config.Name("Dynamic Rain - Smaller, Clearer Drops")
		@Config.Comment("Use my rain texture. I thought the vanilla one was ugly. (Requires dynamic rain to be on)")
		public  boolean custom_texture = true;
		@Config.Name("Dynamic Rain/Snow Draw Distance (Fancy)")
		@Config.RangeInt(min=0,max=255)
		public  int fancy_rain = 40;
		@Config.Name("Dynamic Rain/Snow Draw Distance (Fast)")
		@Config.RangeInt(min=0,max=255)
		public  int fast_rain = 20;
	}
	
	@Config.Name("Friend List")
	public static String[] friends = new String[0];

	@Config.Name("Shader")
	@Config.Comment("Name of shader file, found in /shaders/post/<shader>.json in the minecraft jar file, or in a resource pack. Allows for the shaders used in the removed Super Secret Settings (MC 1.7-1.8)")
	public static String shader = "";
	
	@Config.Comment("You read it right, you can now set your render distance maximum value to whatever you want! But you should make sure that your computer can handle it. Beefy graphics card recommended!")
	@Config.Name("Maximum Render Distance")
	@Config.RangeInt(min=16)
	public static int max_render_distance = 64;
	
	//@Config.Name("A random string...")
	//@Config.Comment("This is here for testing purposes! Feel free to change it to whatever you want! Now you are curious about what it actually did... This string was used to replace player UUIDs in cape selection, instead of using actual player UUID. It does nothing now!")
	//public static String uuid = "hello";

	@Mod.EventBusSubscriber
	static class ConfigurationHolder {

		private static final MethodHandle CONFIGS_GETTER = findFieldGetter(ConfigManager.class, "CONFIGS");


		private static Configuration configuration;


		static Configuration getConfiguration() {
			if (configuration == null) {
				try
				{
					final String fileName = SuperClientInfo.id + ".cfg";

					final Map<String, Configuration> configsMap = (Map<String, Configuration>) CONFIGS_GETTER.invokeExact();

					final Optional<Map.Entry<String, Configuration>> entryOptional = configsMap.entrySet().stream()
							.filter(entry -> fileName.equals(new File(entry.getKey()).getName()))
							.findFirst();

					if (entryOptional.isPresent())
					{
						configuration = entryOptional.get().getValue();
					}
				}
				catch (Throwable throwable)
				{
					System.out.println("Failed to get Configuration :(");
				}
			}

			return configuration;
		}

		/**
		 * Inject the new values and save to the config file when the config has been changed from the GUI.
		 *
		 * @param event The event
		 */
		@SubscribeEvent
		public static void onConfigChanged(ConfigChangedEvent.OnConfigChangedEvent event)
		{
			if (event.getModID().equals(SuperClientInfo.id))
			{
				ConfigManager.load(SuperClientInfo.id, Config.Type.INSTANCE);
				SuperClient.toggle = af.autofunctions;
				SuperClient.refreshDesc();
				new File("superclient/coords/"+display.coords.saved_coordinate_profile).mkdirs();
				SuperClientEvent.loadCoords();
				SuperClient.refreshDesc();
				SuperClient.playerRenderer();
				SuperClient.entityRenderer();
				SuperClient.fontRenderer();
				GameSettings.Options.RENDER_DISTANCE.setValueMax(max_render_distance);
				if (Minecraft.getMinecraft().gameSettings.renderDistanceChunks > max_render_distance)
					Minecraft.getMinecraft().gameSettings.renderDistanceChunks = max_render_distance;
				if (!shader.equals(""))
					Minecraft.getMinecraft().entityRenderer.loadShader(new ResourceLocation("shaders/post/" + shader + ".json"));
				SuperClient.wordSwap();
			}
		}

		public static MethodHandle findFieldGetter(Class<?> clazz, String... fieldNames)
		{
			final Field field = ReflectionHelper.findField(clazz, fieldNames);

			try
			{
				return MethodHandles.lookup().unreflectGetter(field);
			}
			catch (IllegalAccessException e)
			{
				throw new ReflectionHelper.UnableToAccessFieldException(fieldNames, e);
			}
		}
	}

	
}