package snownee.kiwi;

import snownee.kiwi.config.KiwiConfig;
import snownee.kiwi.config.KiwiConfig.Comment;
import snownee.kiwi.config.KiwiConfig.ConfigType;
import snownee.kiwi.config.KiwiConfig.Path;
import snownee.kiwi.config.KiwiConfig.Range;
import snownee.kiwi.config.KiwiConfig.Translation;

@KiwiConfig(type = ConfigType.CLIENT)
public final class KiwiClientConfig {

	public static String contributorCosmetic = "";

	@Comment("Show customized tooltips from any item. Mainly for pack devs")
	@Translation("globalTooltip")
	public static boolean globalTooltip = false;

	@Comment("Max line width shown in description of tooltips")
	@Translation("tooltipWrapWidth")
	@Range(min = 50)
	public static int tooltipWrapWidth = 200;

	public static boolean noMicrosoftTelemetry = true;

	@Comment("Show item tags in advanced tooltips")
	@Translation("tagsTooltip")
	@Path("debug.tagsTooltip")
	public static boolean tagsTooltip = true;

	@Comment("Show item nbt in advanced tooltips while holding shift")
	@Translation("nbtTooltip")
	@Path("debug.nbtTooltip")
	public static boolean nbtTooltip = true;

	@Comment("Allowed values: vanilla, kiwi")
	@Translation("tooltipNBTFormatter")
	@Path("debug.tooltipNBTFormatter")
	public static String debugTooltipNBTFormatter = "vanilla";

	@Comment("Show tips about disabling debug tooltips")
	@Path("debug.debugTooltipMsg")
	public static boolean debugTooltipMsg = true;

}
