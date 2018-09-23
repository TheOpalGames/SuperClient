package net.theopalgames.superclient.hud;

import java.io.IOException;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.theopalgames.superclient.SuperClientEvent;

@SideOnly(Side.CLIENT)
public class SuperClientEditMenu extends GuiScreen
{
	public static SuperClientHUD hud = new SuperClientHUD();
    /**
     * Adds the buttons (and other controls) to the screen in question. Called when the GUI is displayed and when the
     * window resizes, the buttonList is cleared beforehand.
     */
	boolean showHelp = true;
    public void initGui()
    {
        
    }

    /**
     * Called by the controls from the buttonList when activated. (Mouse pressed for buttons)
     */
    protected void actionPerformed(GuiButton button) throws IOException
    {
        
    }

    /**
     * Called when the screen is unloaded. Used to disable keyboard repeat events
     */
    public void onGuiClosed()
    {
    	for (Moveable m: Moveable.moveables)
    		m.save();
    }
    
    /**
     * Called from the main game loop to update the screen.
     */
    public void updateScreen()
    {
    	ScaledResolution s = new ScaledResolution(Minecraft.getMinecraft());
    	SuperClientEvent.instance.width = s.getScaledWidth();
    	SuperClientEvent.instance.height = s.getScaledHeight();

    }

    /**
     * Draws the screen and all the components in it.
     */
    @SuppressWarnings("static-access")
	public void drawScreen(int mouseX, int mouseY, float partialTicks)
    {
		
        //this.drawDefaultBackground();
		drawRect(0, 0, width, height, 0x7f000000);
		
    	//ScaledResolution screen = new ScaledResolution(mc);
		//int width = screen.getScaledWidth();
		//int height = screen.getScaledHeight();
		
		drawRect(width / 3, 0, width / 3 + 1, height, 0xff000000);
		drawRect(2 * width / 3 - 1, 0, 2 * width / 3, height, 0xff000000);
		drawRect(0, height / 3, width, height / 3 + 1, 0xff000000);
		drawRect(0, 2 * height / 3 - 1, width, 2 * height / 3, 0xff000000);
		
		
		
		SuperClientEvent e = SuperClientEvent.instance;
		
    	e.drawArmor();
    	e.drawHeldItems();
    	e.drawUsername();
    	e.drawFPS();
    	e.drawAutofunctions();
    	e.drawTitle();
    	e.drawClickrateL();
    	e.drawClickrateR();
    	e.drawLocation();
    	e.drawSpawnpoint();
    	e.drawSavedCoords();
    	e.drawHotbarNumbers();
    	e.drawStatusEffects();
    	e.drawMoveControls();
    	
    	for (Moveable m: Moveable.moveables)
    	{
    		int[] pos = m.getPos();
    		m.drawBox(pos[0], pos[1]);
    	}
    	
    	if (showHelp)
		{
			SuperClientEvent.instance.drawStringAtPosition(0, 0, 0, -50, "\u00A7lEdit mode", 0x00ffff);

			SuperClientEvent.instance.drawStringAtPosition(0, 0, 0, -30, "Left Click a square to select a text component", 0x00ffff);
			SuperClientEvent.instance.drawStringAtPosition(0, 0, 0, -20, "Drag the selected square to move it", 0x00ffff);
			SuperClientEvent.instance.drawStringAtPosition(0, 0, 0, -10, "Right click a square to toggle visibility", 0x00ffff);
			SuperClientEvent.instance.drawStringAtPosition(0, 0, 0, 0, "Middle click a square to reset position to default", 0x00ffff);
			SuperClientEvent.instance.drawStringAtPosition(0, 0, 0, 10, "Text components will auto-align based on position", 0x00ffff);
			SuperClientEvent.instance.drawStringAtPosition(0, 0, 0, 20, "Press Escape or " + SuperClientEvent.instance.keyBindings[23].getDisplayName() + " to save and exit edit mode", 0x00ffff);
			SuperClientEvent.instance.drawStringAtPosition(0, 0, 0, 30, "Click anywhere to dismiss this text", 0x00ffff);

		}
    	
        super.updateScreen();
        super.drawScreen(mouseX, mouseY, partialTicks);
    }
    @Override
    public void mouseClicked(int mouseX, int mouseY, int clickedMouseButton)
    {
    	showHelp = false;
    	
    	if (clickedMouseButton == 0)
		{
    		hud.selectedMoveable = null;
		}
    	for (Moveable m: Moveable.moveables)
    	{
    		
    		int[] pos = m.getPos();
    		if (Math.abs(pos[0] - mouseX) <= 4 && Math.abs(pos[1] - mouseY) <= 4)
    		{
    			if (clickedMouseButton == 0)
    			{
    				hud.selectedMoveable = m;
    				return;
    			}
    			else if (clickedMouseButton == 1)
    			{
    	    		m.show = !m.show;
    	    		return;
    			}
    			else if (clickedMouseButton == 2)
    			{
    	    		m.resetToDefault();
    	    		return;
    			}
    		}
    	}
    }
    @Override
    public void mouseClickMove(int mouseX, int mouseY, int clickedMouseButton, long timeSinceLastClick)
    {
    	if (hud.selectedMoveable != null && clickedMouseButton == 0)
    	{
    		int[] pos = getPos(mouseX, mouseY);
    		if (hud.selectedMoveable == hud.hotbarNumbers)
        		return;
    		hud.selectedMoveable.setPosition(pos[2], pos[3], pos[0], pos[1]);
    	}
    	//SuperClientConfig.display.armor.position.offX = getPos(mouseX, mouseY)[0];
		//SuperClientConfig.display.armor.position.offY = getPos(mouseX, mouseY)[1];
		//SuperClientConfig.display.armor.position.locX = getPos(mouseX, mouseY)[2];
		//SuperClientConfig.display.armor.position.locY = getPos(mouseX, mouseY)[3];

    }

    /**
     * Fired when a key is typed (except F11 which toggles full screen). This is the equivalent of
     * KeyListener.keyTyped(KeyEvent e). Args : character (character on the key), keyCode (lwjgl Keyboard key code)
     */
    @Override
    protected void keyTyped(char typedChar, int keyCode) throws IOException
    {
    	if (keyCode == SuperClientEvent.keyBindings[23].getKeyCode())
    	{
    		this.mc.displayGuiScreen((GuiScreen)null);

            if (this.mc.currentScreen == null)
            {
                this.mc.setIngameFocus();
            }
    	}
		super.keyTyped(typedChar, keyCode);

    }
    int[] getPos(int x, int y)
    {
    	int offX;
    	int offY;
    	int posX;
    	int posY;
    	if (x < width / 3)
    	{
    		posX = -1;
    		offX = x;
    	}
    	else if (x < 2 * width / 3)
    	{
    		posX = 0;
    		offX = x - width / 2;
    	}
    	else
    	{
    		posX = 1;
    		offX = x - width + 1;
    	}
    	
    	if (y < height / 3)
    	{
    		posY = -1;
    		offY = y;
    	}
    	else if (y < 2 * height / 3)
    	{
    		posY = 0;
    		offY = y - height / 2;
    	}
    	else
    	{
    		posY = 1;
    		offY = y - height + 1;
    	}
    	return new int[]{offX, offY, posX, posY};
    		
    }

    
}