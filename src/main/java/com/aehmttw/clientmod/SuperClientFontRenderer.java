package com.aehmttw.clientmod;

import com.ibm.icu.text.ArabicShaping;
import com.ibm.icu.text.ArabicShapingException;
import com.ibm.icu.text.Bidi;
import java.awt.image.BufferedImage;
import java.io.Closeable;
import java.io.IOException;
import java.util.Locale;
import java.util.Random;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.client.renderer.texture.TextureUtil;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.resources.IResource;
import net.minecraft.client.resources.IResourceManager;
import net.minecraft.client.resources.IResourceManagerReloadListener;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.fml.relauncher.ReflectionHelper;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.apache.commons.io.IOUtils;

@SideOnly(Side.CLIENT)
public class SuperClientFontRenderer extends FontRenderer implements IResourceManagerReloadListener
{
	private static final ResourceLocation[] UNICODE_PAGE_LOCATIONS = new ResourceLocation[256];
	/** Array of width of all the characters in default.png */
	protected int[] charWidth = new int[256];
	/** the height in pixels of default text */
	public int FONT_HEIGHT = 9;
	public Random fontRandom = new Random();
	/** Array of the start/end column (in upper/lower nibble) for every glyph in the /font directory. */
	protected final byte[] glyphWidth = new byte[65536];
	/**
	 * Array of RGB triplets defining the 16 standard chat colors followed by 16 darker version of the same colors for
	 * drop shadows.
	 */
	private final int[] colorCode = new int[32];
	protected final ResourceLocation locationFontTexture;
	/** The RenderEngine used to load and setup glyph textures. */
	@SuppressWarnings("unused")
	private final TextureManager renderEngine;
	/** Current X coordinate at which to draw the next character. */
	protected float posX;
	/** Current Y coordinate at which to draw the next character. */
	protected float posY;
	/** If true, strings should be rendered with Unicode fonts instead of the default.png font */
	private boolean unicodeFlag;
	/** If true, the Unicode Bidirectional Algorithm should be run before rendering any string. */
	private boolean bidiFlag;
	/** Used to specify new red value for the current color. */
	private float red;
	/** Used to specify new blue value for the current color. */
	private float blue;
	/** Used to specify new green value for the current color. */
	private float green;
	/** Used to speify new alpha value for the current color. */
	private float alpha;
	/** Text color of the currently rendering string. */
	private int textColor;
	/** Set if the "k" style (random) is active in currently rendering string */
	private boolean randomStyle;
	/** Set if the "l" style (bold) is active in currently rendering string */
	private boolean boldStyle;
	/** Set if the "o" style (italic) is active in currently rendering string */
	private boolean italicStyle;
	/** Set if the "n" style (underlined) is active in currently rendering string */
	private boolean underlineStyle;
	/** Set if the "m" style (strikethrough) is active in currently rendering string */
	private boolean strikethroughStyle;

	public SuperClientFontRenderer()
	{
		super(Minecraft.getMinecraft().gameSettings, new ResourceLocation("textures/font/ascii.png"), SuperClient.t, SuperClient.u);
		this.locationFontTexture = new ResourceLocation("textures/font/ascii.png");
		this.renderEngine = SuperClient.t;
		this.unicodeFlag = SuperClient.u;
		bindTexture(this.locationFontTexture);

		try
		{
			this.charWidth = ReflectionHelper.getPrivateValue(FontRenderer.class, Minecraft.getMinecraft().fontRenderer, 1);
		}
		catch (Exception e)
		{
			System.out.println("Knock knock... Who's there? Optifine. OPTIFINE WHY ARE YOU STEALING MY CHARACTER WIDTH ARRAY! grrrrrr...");
			this.charWidth = SuperClient.charWidth;
		}
		SuperClient.charWidth = this.charWidth;
		
		this.loadColors();

		this.readGlyphSizes();
	}
	public void loadColors()
	{
		if (!SuperClientConfig.font.colorcodes.customcolor)
			for (int i = 0; i < 32; ++i)
			{
				int j = (i >> 3 & 1) * 85;
				int k = (i >> 2 & 1) * 170 + j;
				int l = (i >> 1 & 1) * 170 + j;
				int i1 = (i >> 0 & 1) * 170 + j;

				if (i == 6)
				{
					k += 85;
				}

				if (Minecraft.getMinecraft().gameSettings.anaglyph)
				{
					int j1 = (k * 30 + l * 59 + i1 * 11) / 100;
					int k1 = (k * 30 + l * 70) / 100;
					int l1 = (k * 30 + i1 * 70) / 100;
					k = j1;
					l = k1;
					i1 = l1;
				}

				if (i >= 16)
				{
					k *= SuperClientConfig.font.shadowmultiplier;
					l *= SuperClientConfig.font.shadowmultiplier;
					i1 *= SuperClientConfig.font.shadowmultiplier;
				}

				this.colorCode[i] = (k & 255) << 16 | (l & 255) << 8 | i1 & 255;
			}
		else
		{
			this.colorCode[0] = SuperClientEvent.parseColor(SuperClientConfig.font.colorcodes.color0);
			this.colorCode[1] = SuperClientEvent.parseColor(SuperClientConfig.font.colorcodes.color1);
			this.colorCode[2] = SuperClientEvent.parseColor(SuperClientConfig.font.colorcodes.color2);
			this.colorCode[3] = SuperClientEvent.parseColor(SuperClientConfig.font.colorcodes.color3);
			this.colorCode[4] = SuperClientEvent.parseColor(SuperClientConfig.font.colorcodes.color4);
			this.colorCode[5] = SuperClientEvent.parseColor(SuperClientConfig.font.colorcodes.color5);
			this.colorCode[6] = SuperClientEvent.parseColor(SuperClientConfig.font.colorcodes.color6);
			this.colorCode[7] = SuperClientEvent.parseColor(SuperClientConfig.font.colorcodes.color7);
			this.colorCode[8] = SuperClientEvent.parseColor(SuperClientConfig.font.colorcodes.color8);
			this.colorCode[9] = SuperClientEvent.parseColor(SuperClientConfig.font.colorcodes.color9);
			this.colorCode[10] = SuperClientEvent.parseColor(SuperClientConfig.font.colorcodes.color10);
			this.colorCode[11] = SuperClientEvent.parseColor(SuperClientConfig.font.colorcodes.color11);
			this.colorCode[12] = SuperClientEvent.parseColor(SuperClientConfig.font.colorcodes.color12);
			this.colorCode[13] = SuperClientEvent.parseColor(SuperClientConfig.font.colorcodes.color13);
			this.colorCode[14] = SuperClientEvent.parseColor(SuperClientConfig.font.colorcodes.color14);
			this.colorCode[15] = SuperClientEvent.parseColor(SuperClientConfig.font.colorcodes.color15);
			if (SuperClientConfig.font.colorcodes.shadowcolor)
			{
				this.colorCode[0+16] = SuperClientEvent.parseColor(SuperClientConfig.font.colorcodes.color0s);
				this.colorCode[1+16] = SuperClientEvent.parseColor(SuperClientConfig.font.colorcodes.color1s);
				this.colorCode[2+16] = SuperClientEvent.parseColor(SuperClientConfig.font.colorcodes.color2s);
				this.colorCode[3+16] = SuperClientEvent.parseColor(SuperClientConfig.font.colorcodes.color3s);
				this.colorCode[4+16] = SuperClientEvent.parseColor(SuperClientConfig.font.colorcodes.color4s);
				this.colorCode[5+16] = SuperClientEvent.parseColor(SuperClientConfig.font.colorcodes.color5s);
				this.colorCode[6+16] = SuperClientEvent.parseColor(SuperClientConfig.font.colorcodes.color6s);
				this.colorCode[7+16] = SuperClientEvent.parseColor(SuperClientConfig.font.colorcodes.color7s);
				this.colorCode[8+16] = SuperClientEvent.parseColor(SuperClientConfig.font.colorcodes.color8s);
				this.colorCode[9+16] = SuperClientEvent.parseColor(SuperClientConfig.font.colorcodes.color9s);
				this.colorCode[10+16] = SuperClientEvent.parseColor(SuperClientConfig.font.colorcodes.color10s);
				this.colorCode[11+16] = SuperClientEvent.parseColor(SuperClientConfig.font.colorcodes.color11s);
				this.colorCode[12+16] = SuperClientEvent.parseColor(SuperClientConfig.font.colorcodes.color12s);
				this.colorCode[13+16] = SuperClientEvent.parseColor(SuperClientConfig.font.colorcodes.color13s);
				this.colorCode[14+16] = SuperClientEvent.parseColor(SuperClientConfig.font.colorcodes.color14s);
				this.colorCode[15+16] = SuperClientEvent.parseColor(SuperClientConfig.font.colorcodes.color15s);
			}
			else
			{
				for (int i = 0; i <= 15; i++)
				{
					int b = this.colorCode[i]%256;
					int g = (this.colorCode[i]/256)%256;
					int r = (this.colorCode[i]/256/256)%256;
					this.colorCode[i+16] = (int)Math.min(255, r*SuperClientConfig.font.shadowmultiplier)*256*256+(int)Math.min(255, g*SuperClientConfig.font.shadowmultiplier)*256+(int)Math.min(255, b*SuperClientConfig.font.shadowmultiplier);
				}
			}
			
		}
	}
	public void onResourceManagerReload(IResourceManager resourceManager)
	{
		this.readFontTexture();
		this.readGlyphSizes();
	}

	private void readFontTexture()
	{
		IResource iresource = null;
		BufferedImage bufferedimage;

		try
		{
			iresource = getResource(this.locationFontTexture);
			bufferedimage = TextureUtil.readBufferedImage(iresource.getInputStream());
		}
		catch (IOException ioexception)
		{
			throw new RuntimeException(ioexception);
		}
		finally
		{
			IOUtils.closeQuietly((Closeable)iresource);
		}

		int lvt_3_2_ = bufferedimage.getWidth();
		int lvt_4_1_ = bufferedimage.getHeight();
		int[] lvt_5_1_ = new int[lvt_3_2_ * lvt_4_1_];
		bufferedimage.getRGB(0, 0, lvt_3_2_, lvt_4_1_, lvt_5_1_, 0, lvt_3_2_);
		int lvt_6_1_ = lvt_4_1_ / 16;
		int lvt_7_1_ = lvt_3_2_ / 16;
		//boolean lvt_8_1_ = true;
		float lvt_9_1_ = 8.0F / (float)lvt_7_1_;

		for (int lvt_10_1_ = 0; lvt_10_1_ < 256; ++lvt_10_1_)
		{
			int j1 = lvt_10_1_ % 16;
			int k1 = lvt_10_1_ / 16;

			if (lvt_10_1_ == 32)
			{
				this.charWidth[lvt_10_1_] = 4;
			}

			int l1;

			for (l1 = lvt_7_1_ - 1; l1 >= 0; --l1)
			{
				int i2 = j1 * lvt_7_1_ + l1;
				boolean flag1 = true;

				for (int j2 = 0; j2 < lvt_6_1_ && flag1; ++j2)
				{
					int k2 = (k1 * lvt_7_1_ + j2) * lvt_3_2_;

					if ((lvt_5_1_[i2 + k2] >> 24 & 255) != 0)
					{
						flag1 = false;
					}
				}

				if (!flag1)
				{
					break;
				}
			}

			++l1;
			this.charWidth[lvt_10_1_] = (int)(0.5D + (double)((float)l1 * lvt_9_1_)) + 1;
		}
	}

	private void readGlyphSizes()
	{
		IResource iresource = null;

		try
		{
			iresource = getResource(new ResourceLocation("font/glyph_sizes.bin"));
			iresource.getInputStream().read(this.glyphWidth);
		}
		catch (IOException ioexception)
		{
			throw new RuntimeException(ioexception);
		}
		finally
		{
			IOUtils.closeQuietly((Closeable)iresource);
		}
	}

	/**
	 * Render the given char
	 */
	private float renderChar(char ch, boolean italic)
	{
		if (ch == 160) return 4.0F; // forge: display nbsp as space. MC-2595
		if (ch == ' ')
		{
			return 4.0F;
		}
		else
		{
			int i = "\u00c0\u00c1\u00c2\u00c8\u00ca\u00cb\u00cd\u00d3\u00d4\u00d5\u00da\u00df\u00e3\u00f5\u011f\u0130\u0131\u0152\u0153\u015e\u015f\u0174\u0175\u017e\u0207\u0000\u0000\u0000\u0000\u0000\u0000\u0000 !\"#$%&'()*+,-./0123456789:;<=>?@ABCDEFGHIJKLMNOPQRSTUVWXYZ[\\]^_`abcdefghijklmnopqrstuvwxyz{|}~\u0000\u00c7\u00fc\u00e9\u00e2\u00e4\u00e0\u00e5\u00e7\u00ea\u00eb\u00e8\u00ef\u00ee\u00ec\u00c4\u00c5\u00c9\u00e6\u00c6\u00f4\u00f6\u00f2\u00fb\u00f9\u00ff\u00d6\u00dc\u00f8\u00a3\u00d8\u00d7\u0192\u00e1\u00ed\u00f3\u00fa\u00f1\u00d1\u00aa\u00ba\u00bf\u00ae\u00ac\u00bd\u00bc\u00a1\u00ab\u00bb\u2591\u2592\u2593\u2502\u2524\u2561\u2562\u2556\u2555\u2563\u2551\u2557\u255d\u255c\u255b\u2510\u2514\u2534\u252c\u251c\u2500\u253c\u255e\u255f\u255a\u2554\u2569\u2566\u2560\u2550\u256c\u2567\u2568\u2564\u2565\u2559\u2558\u2552\u2553\u256b\u256a\u2518\u250c\u2588\u2584\u258c\u2590\u2580\u03b1\u03b2\u0393\u03c0\u03a3\u03c3\u03bc\u03c4\u03a6\u0398\u03a9\u03b4\u221e\u2205\u2208\u2229\u2261\u00b1\u2265\u2264\u2320\u2321\u00f7\u2248\u00b0\u2219\u00b7\u221a\u207f\u00b2\u25a0\u0000".indexOf(ch);
			return i != -1 && !this.unicodeFlag ? this.renderDefaultChar(i, italic) : this.renderUnicodeChar(ch, italic);
		}
	}

	/**
	 * Render a single character with the default.png font at current (posX,posY) location...
	 */
	protected float renderDefaultChar(int ch, boolean italic)
	{
		int i = ch % 16 * 8;
		int j = ch / 16 * 8;
		int k = italic ? 1 : 0;
		bindTexture(this.locationFontTexture);
		int l = this.charWidth[ch];
		float f = (float)l - 0.01F;
		GlStateManager.glBegin(5);
		GlStateManager.glTexCoord2f((float)i / 128.0F, (float)j / 128.0F);
		GlStateManager.glVertex3f(this.posX + (float)k, this.posY, 0.0F);
		GlStateManager.glTexCoord2f((float)i / 128.0F, ((float)j + 7.99F) / 128.0F);
		GlStateManager.glVertex3f(this.posX - (float)k, this.posY + 7.99F, 0.0F);
		GlStateManager.glTexCoord2f(((float)i + f - 1.0F) / 128.0F, (float)j / 128.0F);
		GlStateManager.glVertex3f(this.posX + f - 1.0F + (float)k, this.posY, 0.0F);
		GlStateManager.glTexCoord2f(((float)i + f - 1.0F) / 128.0F, ((float)j + 7.99F) / 128.0F);
		GlStateManager.glVertex3f(this.posX + f - 1.0F - (float)k, this.posY + 7.99F, 0.0F);
		GlStateManager.glEnd();
		return (float)l;
	}

	private ResourceLocation getUnicodePageLocation(int page)
	{
		if (UNICODE_PAGE_LOCATIONS[page] == null)
		{
			UNICODE_PAGE_LOCATIONS[page] = new ResourceLocation(String.format("textures/font/unicode_page_%02x.png", page));
		}

		return UNICODE_PAGE_LOCATIONS[page];
	}

	/**
	 * Load one of the /font/glyph_XX.png into a new GL texture and store the texture ID in glyphTextureName array.
	 */
	private void loadGlyphTexture(int page)
	{
		bindTexture(this.getUnicodePageLocation(page));
	}

	/**
	 * Render a single Unicode character at current (posX,posY) location using one of the /font/glyph_XX.png files...
	 */
	protected float renderUnicodeChar(char ch, boolean italic)
	{
		int i = this.glyphWidth[ch] & 255;

		if (i == 0)
		{
			return 0.0F;
		}
		else
		{
			int j = ch / 256;
			this.loadGlyphTexture(j);
			int k = i >>> 4;
				int l = i & 15;
				float f = (float)k;
				float f1 = (float)(l + 1);
				float f2 = (float)(ch % 16 * 16) + f;
				float f3 = (float)((ch & 255) / 16 * 16);
				float f4 = f1 - f - 0.02F;
				float f5 = italic ? 1.0F : 0.0F;
				GlStateManager.glBegin(5);
				GlStateManager.glTexCoord2f(f2 / 256.0F, f3 / 256.0F);
				GlStateManager.glVertex3f(this.posX + f5, this.posY, 0.0F);
				GlStateManager.glTexCoord2f(f2 / 256.0F, (f3 + 15.98F) / 256.0F);
				GlStateManager.glVertex3f(this.posX - f5, this.posY + 7.99F, 0.0F);
				GlStateManager.glTexCoord2f((f2 + f4) / 256.0F, f3 / 256.0F);
				GlStateManager.glVertex3f(this.posX + f4 / 2.0F + f5, this.posY, 0.0F);
				GlStateManager.glTexCoord2f((f2 + f4) / 256.0F, (f3 + 15.98F) / 256.0F);
				GlStateManager.glVertex3f(this.posX + f4 / 2.0F - f5, this.posY + 7.99F, 0.0F);
				GlStateManager.glEnd();
				return (f1 - f) / 2.0F + 1.0F;
		}
	}

	/**
	 * Draws the specified string with a shadow.
	 */
	public int drawStringWithShadow(String text, float x, float y, int color)
	{
		return this.drawString(text, x, y, color, true);
	}

	/**
	 * Draws the specified string.
	 */
	public int drawString(String text, int x, int y, int color)
	{
		return this.drawString(text, (float)x, (float)y, color, false);
	}

	/**
	 * Draws the specified string.
	 */
	public int drawString(String text, float x, float y, int color, boolean dropShadow)
	{
		if (SuperClientConfig.font.other.rainbowmode && color != 0)
		{
			int alpha = color/256/256/256;
			color = alpha*256*256*256+getRainbowColor(0);
		}
			
		if (SuperClientConfig.font.other.nonify)
			text = text.replace("\u00A7b[MVP] aehmttw", "\u00A77aehmttw");

		for (String[] s: SuperClient.swaplist)
		{
			text = text.replace(s[0], s[1]);
		}

		enableAlpha();
		this.resetStyles();
		int i = 0;

		if (dropShadow)
		{
			if (this.unicodeFlag)
				i = this.renderString(text, x + 1f, y + 1f, color, true);
			else
				for (float j = 1.0f / SuperClientConfig.font.frequency; j <= SuperClientConfig.font.shadowlength; j+= 1.0f/SuperClientConfig.font.frequency)
				{	
					i = this.renderString(text, x + j * (float)SuperClientConfig.font.shadowX, y + j * (float)SuperClientConfig.font.shadowY, color, true);
				}
			i = Math.max(i, this.renderString(text, x, y, color, false));
		}
		else
		{
			i = this.renderString(text, x, y, color, false);
		}

		return i;
	}
	public int getRainbowColor(int offset)
	{
		/*int cBlue = color % 256;
		int cGreen = (color/256) % 256;
		int cRed = (color/256/256) % 256;
		double brightness = (cBlue + cGreen + cRed) / 3.0 / 255.0;
*/
		int i = (int)(((System.currentTimeMillis()*SuperClientConfig.font.other.rainbowspeed) + offset) % (255*6));
		int red = 0;
		int green = 0;
		int blue = 0;

		if (i < 256)
		{
			red = 255;
			blue = 255-i;
		}
		else if (i < 256*2)
		{
			red = 255;
			green = i-255;
		}
		else if (i < 256*3)
		{
			red = 256*3-1-i;
			green = 255;
		}
		else if (i < 256*4)
		{
			green = 255;
			blue = i-256*3-1;
		}
		else if (i < 256*5)
		{
			green = 256*5-1-i;
			blue = 255;
		}
		else if (i < 256*6)
		{
			red = i-256*5-1;
			blue = 255;
		}
		red = MathHelper.clamp(red, 0, 255);
		green = MathHelper.clamp(green, 0, 255);
		blue = MathHelper.clamp(blue, 0, 255);

		return (int)(red)*256*256 + (int)(green)*256 + (int)(blue);
	}
	public int getGlintColor(int offset, int color)
	{
		int blue = color % 256;
		int green = (color/256) % 256;
		int red = (color/256/256) % 256;
		int increase = 0;
		//double brightness = (cBlue + cGreen + cRed) / 3.0 / 255.0;

		int i = (int)(((System.currentTimeMillis()*SuperClientConfig.font.other.rainbowspeed) + offset) % (1000));

		if (i <= 50)
		{
			increase = i;
		}
		else if (i < 100)
		{
			increase = 100 - i;
		}
		else 
		{
			increase = 0;
		}
		red = MathHelper.clamp(red + increase, 0, 255);
		green = MathHelper.clamp(green + increase, 0, 255);
		blue = MathHelper.clamp(blue + increase, 0, 255);

		return (int)(red)*256*256 + (int)(green)*256 + (int)(blue);
	}
	/**
	 * Apply Unicode Bidirectional Algorithm to string and return a new possibly reordered string for visual rendering.
	 */
	private String bidiReorder(String text)
	{
		try
		{
			Bidi bidi = new Bidi((new ArabicShaping(8)).shape(text), 127);
			bidi.setReorderingMode(0);
			return bidi.writeReordered(2);
		}
		catch (ArabicShapingException var3)
		{
			return text;
		}
	}

	/**
	 * Reset all style flag fields in the class to false; called at the start of string rendering
	 */
	private void resetStyles()
	{
		this.randomStyle = false;
		this.boldStyle = false;
		this.italicStyle = false;
		this.underlineStyle = false;
		this.strikethroughStyle = false;
	}

	/**
	 * Render a single line string at the current (posX,posY) and update posX
	 */
	private void renderStringAtPos(String text, boolean shadow)
	{
		
		for (int i = 0; i < text.length(); ++i)
		{
			//float oldR = this.red;
			//float oldG = this.green;
			//float oldB = this.blue;
			
			if (SuperClientConfig.font.other.animatedrainbowmode && !(this.red == 0 && this.green == 0 && this.blue == 0))
			{
				int col = getRainbowColor((int)(this.posX*SuperClientConfig.font.other.rainbowsize + this.posY*SuperClientConfig.font.other.rainbowsize));
				this.red = (col/256/256)%256/255f;
				this.green = (col/256)%256/255f;
				this.blue = col%256/255f;
				if (!shadow)
					this.setColor(this.red, this.green, this.blue, this.alpha);
				else
					this.setColor((float)(this.red * SuperClientConfig.font.shadowmultiplier), (float)(this.green * SuperClientConfig.font.shadowmultiplier), (float)(this.blue * SuperClientConfig.font.shadowmultiplier), this.alpha);
			}
			
			
			char c0 = text.charAt(i);

			if (c0 == 167 && i + 1 < text.length())
			{
				int i1 = "0123456789abcdefklmnor".indexOf(String.valueOf(text.charAt(i + 1)).toLowerCase(Locale.ROOT).charAt(0));

				if (i1 < 16)
				{
					this.randomStyle = false;
					this.boldStyle = false;
					this.strikethroughStyle = false;
					this.underlineStyle = false;
					this.italicStyle = false;

					if (i1 < 0 || i1 > 15)
					{
						i1 = 15;
					}

					if (shadow)
					{
						i1 += 16;
					}

					int j1 = this.colorCode[i1];
					this.textColor = j1;
					//oldR = (float)(j1 >> 16) / 255.0F;
					//oldG = (float)(j1 >> 8 & 255) / 255.0F;
					//oldB =  (float)(j1 & 255) / 255.0F;
					setColor((float)(j1 >> 16) / 255.0F, (float)(j1 >> 8 & 255) / 255.0F, (float)(j1 & 255) / 255.0F, this.alpha);
				}
				else if (i1 == 16)
				{
					this.randomStyle = true;
				}
				else if (i1 == 17)
				{
					this.boldStyle = true;
				}
				else if (i1 == 18)
				{
					this.strikethroughStyle = true;
				}
				else if (i1 == 19)
				{
					this.underlineStyle = true;
				}
				else if (i1 == 20)
				{
					this.italicStyle = true;
				}
				else if (i1 == 21)
				{
					this.randomStyle = false;
					this.boldStyle = false;
					this.strikethroughStyle = false;
					this.underlineStyle = false;
					this.italicStyle = false;
					
					//oldR = this.red;
					//oldG = this.green;
					//oldB = this.blue;
					setColor(this.red, this.blue, this.green, this.alpha);
				}

				++i;
			}
			else
			{
				int j = "\u00c0\u00c1\u00c2\u00c8\u00ca\u00cb\u00cd\u00d3\u00d4\u00d5\u00da\u00df\u00e3\u00f5\u011f\u0130\u0131\u0152\u0153\u015e\u015f\u0174\u0175\u017e\u0207\u0000\u0000\u0000\u0000\u0000\u0000\u0000 !\"#$%&'()*+,-./0123456789:;<=>?@ABCDEFGHIJKLMNOPQRSTUVWXYZ[\\]^_`abcdefghijklmnopqrstuvwxyz{|}~\u0000\u00c7\u00fc\u00e9\u00e2\u00e4\u00e0\u00e5\u00e7\u00ea\u00eb\u00e8\u00ef\u00ee\u00ec\u00c4\u00c5\u00c9\u00e6\u00c6\u00f4\u00f6\u00f2\u00fb\u00f9\u00ff\u00d6\u00dc\u00f8\u00a3\u00d8\u00d7\u0192\u00e1\u00ed\u00f3\u00fa\u00f1\u00d1\u00aa\u00ba\u00bf\u00ae\u00ac\u00bd\u00bc\u00a1\u00ab\u00bb\u2591\u2592\u2593\u2502\u2524\u2561\u2562\u2556\u2555\u2563\u2551\u2557\u255d\u255c\u255b\u2510\u2514\u2534\u252c\u251c\u2500\u253c\u255e\u255f\u255a\u2554\u2569\u2566\u2560\u2550\u256c\u2567\u2568\u2564\u2565\u2559\u2558\u2552\u2553\u256b\u256a\u2518\u250c\u2588\u2584\u258c\u2590\u2580\u03b1\u03b2\u0393\u03c0\u03a3\u03c3\u03bc\u03c4\u03a6\u0398\u03a9\u03b4\u221e\u2205\u2208\u2229\u2261\u00b1\u2265\u2264\u2320\u2321\u00f7\u2248\u00b0\u2219\u00b7\u221a\u207f\u00b2\u25a0\u0000".indexOf(c0);

				if (this.randomStyle && j != -1)
				{
					int k = this.getCharWidth(c0);
					char c1;

					while (true)
					{
						j = this.fontRandom.nextInt("\u00c0\u00c1\u00c2\u00c8\u00ca\u00cb\u00cd\u00d3\u00d4\u00d5\u00da\u00df\u00e3\u00f5\u011f\u0130\u0131\u0152\u0153\u015e\u015f\u0174\u0175\u017e\u0207\u0000\u0000\u0000\u0000\u0000\u0000\u0000 !\"#$%&'()*+,-./0123456789:;<=>?@ABCDEFGHIJKLMNOPQRSTUVWXYZ[\\]^_`abcdefghijklmnopqrstuvwxyz{|}~\u0000\u00c7\u00fc\u00e9\u00e2\u00e4\u00e0\u00e5\u00e7\u00ea\u00eb\u00e8\u00ef\u00ee\u00ec\u00c4\u00c5\u00c9\u00e6\u00c6\u00f4\u00f6\u00f2\u00fb\u00f9\u00ff\u00d6\u00dc\u00f8\u00a3\u00d8\u00d7\u0192\u00e1\u00ed\u00f3\u00fa\u00f1\u00d1\u00aa\u00ba\u00bf\u00ae\u00ac\u00bd\u00bc\u00a1\u00ab\u00bb\u2591\u2592\u2593\u2502\u2524\u2561\u2562\u2556\u2555\u2563\u2551\u2557\u255d\u255c\u255b\u2510\u2514\u2534\u252c\u251c\u2500\u253c\u255e\u255f\u255a\u2554\u2569\u2566\u2560\u2550\u256c\u2567\u2568\u2564\u2565\u2559\u2558\u2552\u2553\u256b\u256a\u2518\u250c\u2588\u2584\u258c\u2590\u2580\u03b1\u03b2\u0393\u03c0\u03a3\u03c3\u03bc\u03c4\u03a6\u0398\u03a9\u03b4\u221e\u2205\u2208\u2229\u2261\u00b1\u2265\u2264\u2320\u2321\u00f7\u2248\u00b0\u2219\u00b7\u221a\u207f\u00b2\u25a0\u0000".length());
						c1 = "\u00c0\u00c1\u00c2\u00c8\u00ca\u00cb\u00cd\u00d3\u00d4\u00d5\u00da\u00df\u00e3\u00f5\u011f\u0130\u0131\u0152\u0153\u015e\u015f\u0174\u0175\u017e\u0207\u0000\u0000\u0000\u0000\u0000\u0000\u0000 !\"#$%&'()*+,-./0123456789:;<=>?@ABCDEFGHIJKLMNOPQRSTUVWXYZ[\\]^_`abcdefghijklmnopqrstuvwxyz{|}~\u0000\u00c7\u00fc\u00e9\u00e2\u00e4\u00e0\u00e5\u00e7\u00ea\u00eb\u00e8\u00ef\u00ee\u00ec\u00c4\u00c5\u00c9\u00e6\u00c6\u00f4\u00f6\u00f2\u00fb\u00f9\u00ff\u00d6\u00dc\u00f8\u00a3\u00d8\u00d7\u0192\u00e1\u00ed\u00f3\u00fa\u00f1\u00d1\u00aa\u00ba\u00bf\u00ae\u00ac\u00bd\u00bc\u00a1\u00ab\u00bb\u2591\u2592\u2593\u2502\u2524\u2561\u2562\u2556\u2555\u2563\u2551\u2557\u255d\u255c\u255b\u2510\u2514\u2534\u252c\u251c\u2500\u253c\u255e\u255f\u255a\u2554\u2569\u2566\u2560\u2550\u256c\u2567\u2568\u2564\u2565\u2559\u2558\u2552\u2553\u256b\u256a\u2518\u250c\u2588\u2584\u258c\u2590\u2580\u03b1\u03b2\u0393\u03c0\u03a3\u03c3\u03bc\u03c4\u03a6\u0398\u03a9\u03b4\u221e\u2205\u2208\u2229\u2261\u00b1\u2265\u2264\u2320\u2321\u00f7\u2248\u00b0\u2219\u00b7\u221a\u207f\u00b2\u25a0\u0000".charAt(j);

						if (k == this.getCharWidth(c1))
						{
							break;
						}
					}

					c0 = c1;
				}

				float f1 = j == -1 || this.unicodeFlag ? 0.5f : 1f;
				boolean flag = (c0 == 0 || j == -1 || this.unicodeFlag) && shadow;

				this.posX += SuperClientConfig.font.spacing-1;

				if (flag)
				{
					this.posX -= f1;
					this.posY -= f1;
				}
				
				
				/*if (SuperClientConfig.font.other.glint)
				{
					int col = getGlintColor((int)(this.posX*SuperClientConfig.font.other.rainbowsize + this.posY*SuperClientConfig.font.other.rainbowsize), (int)(this.red*256*256*255+this.green*256*255+this.blue*255));
					float red = (col/256/256)%256/255f;
					float green = (col/256)%256/255f;
					float blue = col%256/255f;
					if (!shadow)
						this.setColor(red, green, blue, this.alpha);
					else
						this.setColor((float)(red * SuperClientConfig.font.shadowmultiplier), (float)(green * SuperClientConfig.font.shadowmultiplier), (float)(blue * SuperClientConfig.font.shadowmultiplier), this.alpha);
				}*/
				float f = this.renderChar(c0, this.italicStyle);

				if (flag)
				{
					this.posX += f1;
					this.posY += f1;
				}

				if (this.boldStyle)
				{
					//this.posX += f1;

					if (flag)
					{
						this.posX -= f1;
						this.posY -= f1;
					}
					//this.posX-=1;
					double thickness = SuperClientConfig.font.thickness;
					double frequency = SuperClientConfig.font.frequency;
					if (!SuperClientConfig.font.boldfont)
					{
						thickness = 1;
						frequency = 1;
					}
					for (int i2 = 0; i2 < thickness * frequency ; i2++)
					{
						this.posX += 1.0 / frequency;
						this.renderChar(c0, this.italicStyle);
					}
					//this.red = oldR;
					//this.green = oldG;
					//this.blue = oldB;

					this.posX -= f1;

					if (flag)
					{
						this.posX += f1;
						this.posY += f1;
					}

					++f;
				}
				doDraw(f);
			}
		}
	}

	protected void doDraw(float f)
	{
		{
			{

				if (this.strikethroughStyle)
				{
					Tessellator tessellator = Tessellator.getInstance();
					BufferBuilder bufferbuilder = tessellator.getBuffer();
					GlStateManager.disableTexture2D();
					bufferbuilder.begin(7, DefaultVertexFormats.POSITION);
					bufferbuilder.pos((double)this.posX, (double)(this.posY + (float)(this.FONT_HEIGHT / 2)), 0.0D).endVertex();
					bufferbuilder.pos((double)(this.posX + f), (double)(this.posY + (float)(this.FONT_HEIGHT / 2)), 0.0D).endVertex();
					bufferbuilder.pos((double)(this.posX + f), (double)(this.posY + (float)(this.FONT_HEIGHT / 2) - 1.0F), 0.0D).endVertex();
					bufferbuilder.pos((double)this.posX, (double)(this.posY + (float)(this.FONT_HEIGHT / 2) - 1.0F), 0.0D).endVertex();
					tessellator.draw();
					GlStateManager.enableTexture2D();
				}

				if (this.underlineStyle)
				{
					Tessellator tessellator1 = Tessellator.getInstance();
					BufferBuilder bufferbuilder1 = tessellator1.getBuffer();
					GlStateManager.disableTexture2D();
					bufferbuilder1.begin(7, DefaultVertexFormats.POSITION);
					int l = this.underlineStyle ? -1 : 0;
					bufferbuilder1.pos((double)(this.posX + (float)l), (double)(this.posY + (float)this.FONT_HEIGHT), 0.0D).endVertex();
					bufferbuilder1.pos((double)(this.posX + f), (double)(this.posY + (float)this.FONT_HEIGHT), 0.0D).endVertex();
					bufferbuilder1.pos((double)(this.posX + f), (double)(this.posY + (float)this.FONT_HEIGHT - 1.0F), 0.0D).endVertex();
					bufferbuilder1.pos((double)(this.posX + (float)l), (double)(this.posY + (float)this.FONT_HEIGHT - 1.0F), 0.0D).endVertex();
					tessellator1.draw();
					GlStateManager.enableTexture2D();
				}

				this.posX += (float)((int)f);
			}
		}
	}

	/**
	 * Render string either left or right aligned depending on bidiFlag
	 */
	private int renderStringAligned(String text, int x, int y, int width, int color, boolean dropShadow)
	{
		if (this.bidiFlag)
		{
			int i = this.getStringWidth(this.bidiReorder(text));
			x = x + width - i;
		}

		return this.renderString(text, (float)x, (float)y, color, dropShadow);
	}

	/**
	 * Render single line string by setting GL color, current (posX,posY), and calling renderStringAtPos()
	 */
	private int renderString(String text, float x, float y, int color, boolean dropShadow)
	{
		if (text == null)
		{
			return 0;
		}
		else
		{
			
			if (this.bidiFlag)
			{
				text = this.bidiReorder(text);
			}

			if ((color & -67108864) == 0)
			{
				color |= -16777216;
			}
			
			if (dropShadow)
			{

				color = (color & 16579836) | color & -16777216;
				
				
				//System.out.println(color);
				this.red = (float) (SuperClientConfig.font.shadowmultiplier * (float)(color >> 16 & 255) / 255.0F);
				this.blue = (float) (SuperClientConfig.font.shadowmultiplier * (float)(color >> 8 & 255) / 255.0F);
				this.green = (float) (SuperClientConfig.font.shadowmultiplier * (float)(color & 255) / 255.0F);
				this.alpha = (float)(color >> 24 & 255) / 255.0F;
			}
			else
			{
				this.red = (float)(color >> 16 & 255) / 255.0F;
				this.blue = (float)(color >> 8 & 255) / 255.0F;
				this.green = (float)(color & 255) / 255.0F;
				this.alpha = (float)(color >> 24 & 255) / 255.0F;
			}

			
			
			setColor(this.red, this.blue, this.green, this.alpha);
			this.posX = x;
			this.posY = y;
			this.renderStringAtPos(text, dropShadow);
			return (int)this.posX;
		}
	}

	/**
	 * Returns the width of this string. Equivalent of FontMetrics.stringWidth(String s).
	 */
	public int getStringWidth(String text)
	{
		if (text == null)
		{
			return 0;
		}
		else
		{
			for (String[] s: SuperClient.swaplist)
			{
				text = text.replace(s[0], s[1]);
			}
			double i = 0;
			boolean flag = false;

			for (int j = 0; j < text.length(); ++j)
			{
				char c0 = text.charAt(j);
				int k = this.getCharWidth(c0);

				if (k < 0 && j < text.length() - 1)
				{
					++j;
					c0 = text.charAt(j);

					if (c0 != 'l' && c0 != 'L')
					{
						if (c0 == 'r' || c0 == 'R')
						{
							flag = false;
						}
					}
					else
					{
						flag = true;
					}

					k = 0;
				}

				i += k + SuperClientConfig.font.spacing-1;

				if (flag && k > 0)
				{
					++i;
				}
			}

			return (int)i;
		}
	}

	/**
	 * Returns the width of this character as rendered.
	 */
	public int getCharWidth(char character)
	{
		if (character == 160) return 4; // forge: display nbsp as space. MC-2595
		if (character == 167)
		{
			return -1;
		}
		else if (character == ' ')
		{
			return 4;
		}
		else
		{
			int i = "\u00c0\u00c1\u00c2\u00c8\u00ca\u00cb\u00cd\u00d3\u00d4\u00d5\u00da\u00df\u00e3\u00f5\u011f\u0130\u0131\u0152\u0153\u015e\u015f\u0174\u0175\u017e\u0207\u0000\u0000\u0000\u0000\u0000\u0000\u0000 !\"#$%&'()*+,-./0123456789:;<=>?@ABCDEFGHIJKLMNOPQRSTUVWXYZ[\\]^_`abcdefghijklmnopqrstuvwxyz{|}~\u0000\u00c7\u00fc\u00e9\u00e2\u00e4\u00e0\u00e5\u00e7\u00ea\u00eb\u00e8\u00ef\u00ee\u00ec\u00c4\u00c5\u00c9\u00e6\u00c6\u00f4\u00f6\u00f2\u00fb\u00f9\u00ff\u00d6\u00dc\u00f8\u00a3\u00d8\u00d7\u0192\u00e1\u00ed\u00f3\u00fa\u00f1\u00d1\u00aa\u00ba\u00bf\u00ae\u00ac\u00bd\u00bc\u00a1\u00ab\u00bb\u2591\u2592\u2593\u2502\u2524\u2561\u2562\u2556\u2555\u2563\u2551\u2557\u255d\u255c\u255b\u2510\u2514\u2534\u252c\u251c\u2500\u253c\u255e\u255f\u255a\u2554\u2569\u2566\u2560\u2550\u256c\u2567\u2568\u2564\u2565\u2559\u2558\u2552\u2553\u256b\u256a\u2518\u250c\u2588\u2584\u258c\u2590\u2580\u03b1\u03b2\u0393\u03c0\u03a3\u03c3\u03bc\u03c4\u03a6\u0398\u03a9\u03b4\u221e\u2205\u2208\u2229\u2261\u00b1\u2265\u2264\u2320\u2321\u00f7\u2248\u00b0\u2219\u00b7\u221a\u207f\u00b2\u25a0\u0000".indexOf(character);

			if (character > 0 && i != -1 && !this.unicodeFlag)
			{
				return this.charWidth[i];
			}
			else if (this.glyphWidth[character] != 0)
			{
				int j = this.glyphWidth[character] & 255;
				int k = j >>> 4;
				int l = j & 15;
				++l;
				return (l - k) / 2 + 1;
			}
			else
			{
				return 0;
			}
		}
	}

	/**
	 * Trims a string to fit a specified Width.
	 */
	public String trimStringToWidth(String text, int width)
	{
		return this.trimStringToWidth(text, width, false);
	}

	/**
	 * Trims a string to a specified width, optionally starting from the end and working backwards.
	 * <h3>Samples:</h3>
	 * (Assuming that {@link #getCharWidth(char)} returns <code>6</code> for all of the characters in
	 * <code>0123456789</code> on the current resource pack)
	 * <table>
	 * <tr><th>Input</th><th>Returns</th></tr>
	 * <tr><td><code>trimStringToWidth("0123456789", 1, false)</code></td><td><samp>""</samp></td></tr>
	 * <tr><td><code>trimStringToWidth("0123456789", 6, false)</code></td><td><samp>"0"</samp></td></tr>
	 * <tr><td><code>trimStringToWidth("0123456789", 29, false)</code></td><td><samp>"0123"</samp></td></tr>
	 * <tr><td><code>trimStringToWidth("0123456789", 30, false)</code></td><td><samp>"01234"</samp></td></tr>
	 * <tr><td><code>trimStringToWidth("0123456789", 9001, false)</code></td><td><samp>"0123456789"</samp></td></tr>
	 * <tr><td><code>trimStringToWidth("0123456789", 1, true)</code></td><td><samp>""</samp></td></tr>
	 * <tr><td><code>trimStringToWidth("0123456789", 6, true)</code></td><td><samp>"9"</samp></td></tr>
	 * <tr><td><code>trimStringToWidth("0123456789", 29, true)</code></td><td><samp>"6789"</samp></td></tr>
	 * <tr><td><code>trimStringToWidth("0123456789", 30, true)</code></td><td><samp>"56789"</samp></td></tr>
	 * <tr><td><code>trimStringToWidth("0123456789", 9001, true)</code></td><td><samp>"0123456789"</samp></td></tr>
	 * </table>
	 */
	public String trimStringToWidth(String text, int width, boolean reverse)
	{
		StringBuilder stringbuilder = new StringBuilder();
		int i = 0;
		int j = reverse ? text.length() - 1 : 0;
		int k = reverse ? -1 : 1;
		boolean flag = false;
		boolean flag1 = false;

		for (int l = j; l >= 0 && l < text.length() && i < width; l += k)
		{
			char c0 = text.charAt(l);
			int i1 = this.getCharWidth(c0);

			if (flag)
			{
				flag = false;

				if (c0 != 'l' && c0 != 'L')
				{
					if (c0 == 'r' || c0 == 'R')
					{
						flag1 = false;
					}
				}
				else
				{
					flag1 = true;
				}
			}
			else if (i1 < 0)
			{
				flag = true;
			}
			else
			{
				i += i1;

				if (flag1)
				{
					++i;
				}
			}

			if (i > width)
			{
				break;
			}

			if (reverse)
			{
				stringbuilder.insert(0, c0);
			}
			else
			{
				stringbuilder.append(c0);
			}
		}

		return stringbuilder.toString();
	}

	/**
	 * Remove all newline characters from the end of the string
	 */
	private String trimStringNewline(String text)
	{
		for (String[] s: SuperClient.swaplist)
		{
			text = text.replace(s[0], s[1]);
		}
		
		while (text != null && text.endsWith("\n"))
		{
			text = text.substring(0, text.length() - 1);
		}

		return text;
	}

	/**
	 * Splits and draws a String with wordwrap (maximum length is parameter k)
	 */
	public void drawSplitString(String str, int x, int y, int wrapWidth, int textColor)
	{
		this.resetStyles();
		this.textColor = textColor;
		str = this.trimStringNewline(str);
		this.renderSplitString(str, x, y, wrapWidth, false);
	}

	/**
	 * Perform actual work of rendering a multi-line string with wordwrap and with darker drop shadow color if flag is
	 * set
	 */
	private void renderSplitString(String str, int x, int y, int wrapWidth, boolean addShadow)
	{
		for (String s : this.listFormattedStringToWidth(str, wrapWidth))
		{
			this.renderStringAligned(s, x, y, wrapWidth, this.textColor, addShadow);
			y += this.FONT_HEIGHT;
		}
	}

	/**
	 * Returns the height (in pixels) of the given string if it is wordwrapped to the given max width.
	 */
	public int getWordWrappedHeight(String str, int maxLength)
	{
		return this.FONT_HEIGHT * this.listFormattedStringToWidth(str, maxLength).size();
	}

	/**
	 * Set unicodeFlag controlling whether strings should be rendered with Unicode fonts instead of the default.png
	 * font.
	 */
	public void setUnicodeFlag(boolean unicodeFlagIn)
	{
		this.unicodeFlag = unicodeFlagIn;
	}

	/**
	 * Get unicodeFlag controlling whether strings should be rendered with Unicode fonts instead of the default.png
	 * font.
	 */
	public boolean getUnicodeFlag()
	{
		return this.unicodeFlag;
	}

	/**
	 * Set bidiFlag to control if the Unicode Bidirectional Algorithm should be run before rendering any string.
	 */
	public void setBidiFlag(boolean bidiFlagIn)
	{
		this.bidiFlag = bidiFlagIn;
	}

	/**
	 * Breaks a string into a list of pieces where the width of each line is always less than or equal to the provided
	 * width. Formatting codes will be preserved between lines.
	 */
	/*public List<String> listFormattedStringToWidth(String str, int wrapWidth)
    {
        return Arrays.<String>asList(this.wrapFormattedStringToWidth(str, wrapWidth).split("\n"));
    }*/

	/**
	 * Inserts newline and formatting into a string to wrap it within the specified width.
	 */
	/*String wrapFormattedStringToWidth(String str, int wrapWidth)
    {
        int i = this.sizeStringToWidth(str, wrapWidth);

        if (str.length() <= i)
        {
            return str;
        }
        else
        {
            String s = str.substring(0, i);
            char c0 = str.charAt(i);
            boolean flag = c0 == ' ' || c0 == '\n';
            String s1 = getFormatFromString(s) + str.substring(i + (flag ? 1 : 0));
            return s + "\n" + this.wrapFormattedStringToWidth(s1, wrapWidth);
        }
    }*/

	/**
	 * Determines how many characters from the string will fit into the specified width.
	 */
	/*private int sizeStringToWidth(String str, int wrapWidth)
    {
        int i = str.length();
        int j = 0;
        int k = 0;
        int l = -1;

        for (boolean flag = false; k < i; ++k)
        {
            char c0 = str.charAt(k);

            switch (c0)
            {
                case '\n':
                    --k;
                    break;
                case ' ':
                    l = k;
                default:
                    j += this.getCharWidth(c0);

                    if (flag)
                    {
                        ++j;
                    }

                    break;
                case '\u00a7':

                    if (k < i - 1)
                    {
                        ++k;
                        char c1 = str.charAt(k);

                        if (c1 != 'l' && c1 != 'L')
                        {
                            if (c1 == 'r' || c1 == 'R' || isFormatColor(c1))
                            {
                                flag = false;
                            }
                        }
                        else
                        {
                            flag = true;
                        }
                    }
            }

            if (c0 == '\n')
            {
                ++k;
                l = k;
                break;
            }

            if (j > wrapWidth)
            {
                break;
            }
        }

        return k != i && l != -1 && l < k ? l : k;
    }*/

	/**
	 * Checks if the char code is a hexadecimal character, used to set colour.
	 */
	private static boolean isFormatColor(char colorChar)
	{
		return colorChar >= '0' && colorChar <= '9' || colorChar >= 'a' && colorChar <= 'f' || colorChar >= 'A' && colorChar <= 'F';
	}

	/**
	 * Checks if the char code is O-K...lLrRk-o... used to set special formatting.
	 */
	private static boolean isFormatSpecial(char formatChar)
	{
		return formatChar >= 'k' && formatChar <= 'o' || formatChar >= 'K' && formatChar <= 'O' || formatChar == 'r' || formatChar == 'R';
	}

	/**
	 * Digests a string for nonprinting formatting characters then returns a string containing only that formatting.
	 */
	public static String getFormatFromString(String text)
	{
		String s = "";
		int i = -1;
		int j = text.length();

		while ((i = text.indexOf(167, i + 1)) != -1)
		{
			if (i < j - 1)
			{
				char c0 = text.charAt(i + 1);

				if (isFormatColor(c0))
				{
					s = "\u00a7" + c0;
				}
				else if (isFormatSpecial(c0))
				{
					s = s + "\u00a7" + c0;
				}
			}
		}

		return s;
	}

	/**
	 * Get bidiFlag that controls if the Unicode Bidirectional Algorithm should be run before rendering any string
	 */
	public boolean getBidiFlag()
	{
		return this.bidiFlag;
	}

	protected void setColor(float r, float g, float b, float a)
	{
		GlStateManager.color(r,g,b,a);
	}

	protected void enableAlpha()
	{
		GlStateManager.enableAlpha();
	}

	protected void bindTexture(ResourceLocation location)
	{
		//renderEngine.bindTexture(location);
		super.bindTexture(location);
	}

	protected IResource getResource(ResourceLocation location) throws IOException
	{
		return Minecraft.getMinecraft().getResourceManager().getResource(location);
	}

	public int getColorCode(char character)
	{
		int i = "0123456789abcdef".indexOf(character);
		return i >= 0 && i < this.colorCode.length ? this.colorCode[i] : -1;
	}
}