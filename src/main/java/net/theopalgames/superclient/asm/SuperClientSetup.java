package net.theopalgames.superclient.asm;

import java.lang.reflect.Constructor;
import java.util.Map;

import net.minecraft.launchwrapper.ITweaker;
import net.minecraft.launchwrapper.Launch;
import net.minecraft.launchwrapper.LaunchClassLoader;
import net.minecraftforge.fml.relauncher.CoreModManager;
import net.minecraftforge.fml.relauncher.IFMLCallHook;
import net.theopalgames.superclient.classloading.SuperClientTweaker;

public final class SuperClientSetup implements IFMLCallHook {
	public static void init(LaunchClassLoader classLoader) throws Exception {
		addExclusions(classLoader);
		
		Class<? extends ITweaker> pluginWrapper = (Class<? extends ITweaker>) Class.forName("net.minecraftforge.fml.relauncher.CoreModManager$FMLPluginWrapper", true, classLoader);
		Constructor<? extends ITweaker> constructor = (Constructor<? extends ITweaker>) pluginWrapper.getConstructors()[0];
		constructor.setAccessible(true);
		ITweaker tweak = constructor.newInstance("superclient", new SuperClientCorePlugin(), SuperClientTweaker.jarLocation, 9001, new String[0]); // Who cares about the sorting index anyway, the tweaks were already sorted.
		
		tweak.injectIntoClassLoader(classLoader); // Force the FML injection.
	}
	
	@Override
	public Void call() throws Exception {
		if ((Boolean) Launch.blackboard.get("fml.deobfuscatedEnvironment"))
			addExclusions((LaunchClassLoader) getClass().getClassLoader());
		
		hideCoreMod();
		
		return null;
	}
	
	@Override
	public void injectData(Map<String, Object> data) {
		// NOOP
	}
	
	private void hideCoreMod() throws Exception { // No lombok :(
		if (!((Boolean) Launch.blackboard.get("fml.deobfuscatedEnvironment")))
			CoreModManager.getIgnoredMods().remove(SuperClientTweaker.jarLocation.getName());
	}
	
	private static void addExclusions(LaunchClassLoader classLoader) {
		classLoader.addTransformerExclusion("net.theopalgames.superclient.asm.");
		classLoader.addClassLoaderExclusion("net.theopalgames.superclient.classloading.");
	}
	
	private static void initCertificates() {
		switch (System.getProperty("os.name")) {
		case "mac":
			// TODO: Fix the certificates to support Let's Encrypt.
		}
	}
}
