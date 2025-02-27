package snownee.kiwi.loader;

import net.minecraft.server.MinecraftServer;
import net.minecraftforge.data.loading.DatagenModLoader;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.loading.FMLEnvironment;
import net.minecraftforge.server.ServerLifecycleHooks;

public class Platform {

	private Platform() {
	}

	public static boolean isModLoaded(String id) {
		return ModList.get().isLoaded(id);
	}

	public static boolean isPhysicalClient() {
		return FMLEnvironment.dist.isClient();
	}

	public static MinecraftServer getServer() {
		return ServerLifecycleHooks.getCurrentServer();
	}

	public static boolean isProduction() {
		return FMLEnvironment.production;
	}

	public static boolean isDataGen() {
		return DatagenModLoader.isRunningDataGen();
	}

}
