package net.theopalgames.superclient.classloading;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.List;

import org.apache.logging.log4j.core.util.Throwables;

import net.minecraft.launchwrapper.ITweaker;
import net.minecraft.launchwrapper.LaunchClassLoader;

public final class SuperClientTweaker implements ITweaker {
	public static File jarLocation;
	
	@Override
	public void acceptOptions(List<String> args, File gameDir, File assetsDir, String profile) {
		// NOOP
	}
	
	@Override
	public void injectIntoClassLoader(LaunchClassLoader classLoader) {
		URL url = getClass().getProtectionDomain().getCodeSource().getLocation();
		
		try {
			jarLocation = new File(url.toURI());
		} catch (URISyntaxException e) {
			Throwables.rethrow(e);
		}
		
		classLoader.addURL(url);
		
		try {
			Method method = Class.forName("net.theopalgames.superclient.asm.SuperClientSetup").getDeclaredMethod("init", LaunchClassLoader.class);
			method.invoke(null, classLoader);
		} catch (NoSuchMethodException | SecurityException | ClassNotFoundException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
			Throwables.rethrow(e);
		}
	}
	
	@Override
	public String getLaunchTarget() {
		throw new UnsupportedOperationException("Illegal primary tweaker");
	}
	
	@Override
	public String[] getLaunchArguments() {
		return new String[0];
	}
}
