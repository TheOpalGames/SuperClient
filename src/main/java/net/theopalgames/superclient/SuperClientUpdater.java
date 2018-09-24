package net.theopalgames.superclient;

import java.io.File;
import java.util.Arrays;
import java.util.List;

import com.google.common.base.Charsets;
import com.google.common.io.Files;

import io.netty.channel.nio.NioEventLoopGroup;
import net.theopalgames.superclient.classloading.SuperClientLog;
import net.theopalgames.superclient.repackage.ComparableVersion;
import net.theopalgames.superclient.repackage.http.HttpClient;

public final class SuperClientUpdater {
	private static ComparableVersion lastNotice;
	private static ComparableVersion currentVersion;
	private static ComparableVersion latestVersion;
	private static List<String> instructions;
	private static final NioEventLoopGroup group = new NioEventLoopGroup();
	
	public static void checkForUpdates() throws Exception {
		File updateNotice = new File("superclient/tutorial/updateNotice.yummypie");
		if (!updateNotice.exists()) {
			updateNotice.createNewFile();
			Files.asCharSink(updateNotice, Charsets.UTF_8).write("0.0.1"); // So any new version will be later than this.
		}
		
		String contents = Files.asCharSource(updateNotice, Charsets.UTF_8).read();
		
		lastNotice = new ComparableVersion(contents);
		currentVersion = new ComparableVersion(SuperClientInfo.version);
		HttpClient.get("https://hallowizer.com/superclient/latestVersion.yummypie", group, SuperClientUpdater::handleLatestVersion);
		SuperClient.latestVersion = latestVersion;
	}
	
	private static void handleLatestVersion(String result, Throwable error) throws Exception {
		if (error != null) {
			SuperClientLog.log.error("An error occurred while checking for updates.", error);
			return;
		}
		
		latestVersion = new ComparableVersion(result);
		SuperClient.latestVersion = latestVersion;
		
		instructions = SuperClient.instructions;
		if (currentVersion.compareTo(latestVersion) < 0 && lastNotice.compareTo(latestVersion) < 0) {
			instructions.add("Welcome back! There is a new update for The Super Client!");
			instructions.add("You can update to version " + latestVersion.toString() + " now!");
			HttpClient.get("https://hallowizer.com/superclient/changelog.yummypie", group, SuperClientUpdater::handleChangelog);
		}
	}
	
	private static void handleChangelog(String result, Throwable error) throws Exception {
		instructions.addAll(Arrays.asList(result.split("\n")));
		instructions.add("That's all for now, enjoy!");
	}
}
