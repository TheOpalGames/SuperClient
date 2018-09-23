package net.theopalgames.superclient.asm;

import java.util.Map;

import net.minecraftforge.fml.relauncher.IFMLLoadingPlugin;

public final class SuperClientCorePlugin implements IFMLLoadingPlugin {
	@Override
	public String[] getASMTransformerClass() {
		return new String[] {
				
		};
	}
	
	@Override
	public String getModContainerClass() {
		return null; // No thanks, I think I'll just go with the FMLModContainer.
	}
	
	@Override
	public String getSetupClass() {
		return "net.theopalgames.superclient.asm.SuperClientSetup";
	}
	
	@Override
	public void injectData(Map<String, Object> data) {
		// NOOP
	}
	
	@Override
	public String getAccessTransformerClass() {
		return null; // Why does this exist anyway? Can't mods just include their AT rules using the FMLAT manifest attribute?
	}
}
