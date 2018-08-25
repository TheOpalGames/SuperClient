package com.aehmttw.clientmod.hud;

public class SuperClientHUD 
{
	public Moveable armor = new Moveable("armor", true, -1, -1, 1, 1);
	public Moveable heldItems = new Moveable("heldItems", true, 0, -1, 0, 1);
	public Moveable text = new Moveable("text", false, 0, -1, 0, 20);
	public Moveable username = new Moveable("username", true, 1, -1, -1, 1);
	public Moveable framerate = new Moveable("framerate", true, 1, -1, -1, 10);
	public Moveable autoFunctions = new Moveable("autoFunctions", true, 1, -1, -1, 19);
	public Moveable statusEffects = new Moveable("statusEffects", true, -1, 1, 1, -1);
	public Moveable hotbarNumbers = new Moveable("hotbarNumbers", true, 0, 1, 0, -30);
	public Moveable moveControls = new Moveable("moveControls", true, 1, 1, -12, -30);
	public Moveable location = new Moveable("location", true, 1, 1, -1, -1);
	public Moveable spawn = new Moveable("spawn", true, 1, 1, -1, -10);
	public Moveable clickrateL = new Moveable("clickrateL", true, 1, -1, -1, 31);
	public Moveable clickrateR = new Moveable("clickrateR", true, 1, -1, -1, 41);
	public Moveable savedCoords = new Moveable("savedCoords", true, -1, 0, 1, 0);
	
	public Moveable selectedMoveable = null;
}
