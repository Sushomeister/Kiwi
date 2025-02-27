package snownee.kiwi.util;

import java.text.DecimalFormat;
import java.text.MessageFormat;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;

import javax.annotation.Nullable;

import com.google.gson.JsonElement;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.ForgeMod;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.world.BlockEvent.BreakEvent;
import snownee.kiwi.block.def.BlockDefinition;
import snownee.kiwi.loader.Platform;

public final class Util {
	private Util() {
	}

	private static final DecimalFormat DECIMAL_FORMAT = new DecimalFormat("###,###");
	public static final MessageFormat MESSAGE_FORMAT = new MessageFormat("{0,number,#.#}");

	public static String color(int color) {
		return String.format("\u00A7x%06x", color & 0x00FFFFFF);
	}

	public static String formatComma(long number) {
		return DECIMAL_FORMAT.format(number);
	}

	public static String formatCompact(long number) {
		int unit = 1000;
		if (number < unit) {
			return Long.toString(number);
		}
		int exp = (int) (Math.log(number) / Math.log(unit));
		if (exp - 1 >= 0 && exp - 1 < 6) {
			char pre = "kMGTPE".charAt(exp - 1);
			return MESSAGE_FORMAT.format(new Double[] { number / Math.pow(unit, exp) }) + pre;
		}
		return Long.toString(number);
	}

	public static String trimRL(ResourceLocation rl) {
		return trimRL(rl, "minecraft");
	}

	public static String trimRL(String rl) {
		return trimRL(rl, "minecraft");
	}

	/**
	 * @since 2.7.0
	 */
	public static String trimRL(ResourceLocation rl, String defaultNamespace) {
		return rl.getNamespace().equals(defaultNamespace) ? rl.getPath() : rl.toString();
	}

	/**
	 * @since 2.7.0
	 */
	public static String trimRL(String rl, String defaultNamespace) {
		if (rl.startsWith(defaultNamespace + ":")) {
			return rl.substring(defaultNamespace.length() + 1);
		} else {
			return rl;
		}
	}

	@Nullable
	public static ResourceLocation RL(@Nullable String string) {
		try {
			return ResourceLocation.tryParse(string);
		} catch (Exception e) {
			return null;
		}
	}

	/**
	 * @since 2.4.2
	 */
	@Nullable
	public static ResourceLocation RL(@Nullable String string, String defaultNamespace) {
		if (string != null && !string.contains(":")) {
			string = defaultNamespace + ":" + string;
		}
		return RL(string);
	}

	@Nullable
	public static Component getBlockDefName(ItemStack stack, String key) {
		NBTHelper data = NBTHelper.of(stack);
		CompoundTag tag = data.getTag("BlockEntityTag.Overrides." + key);
		if (tag != null) {
			BlockDefinition def = BlockDefinition.fromNBT(tag);
			if (def != null) {
				return def.getDescription();
			}
		}
		return null;
	}

	public static RecipeManager recipeManager;

	@Nullable
	public static RecipeManager getRecipeManager() {
		if (recipeManager != null) {
			return recipeManager;
		} else if (Platform.isPhysicalClient()) {
			ClientPacketListener connection = Minecraft.getInstance().getConnection();
			if (connection != null) {
				return connection.getRecipeManager();
			}
		}
		return null;
	}

	public static <C extends Container, T extends Recipe<C>> List<T> getRecipes(RecipeType<T> recipeTypeIn) {
		RecipeManager manager = getRecipeManager();
		if (manager == null) {
			return Collections.EMPTY_LIST;
		} else {
			return getRecipeManager().getAllRecipesFor(recipeTypeIn);
		}
	}

	public static int friendlyCompare(String a, String b) {
		int aLength = a.length();
		int bLength = b.length();
		int minSize = Math.min(aLength, bLength);
		char aChar, bChar;
		boolean aNumber, bNumber;
		boolean asNumeric = false;
		int lastNumericCompare = 0;
		for (int i = 0; i < minSize; i++) {
			aChar = a.charAt(i);
			bChar = b.charAt(i);
			aNumber = aChar >= '0' && aChar <= '9';
			bNumber = bChar >= '0' && bChar <= '9';
			if (asNumeric)
				if (aNumber && bNumber) {
					if (lastNumericCompare == 0)
						lastNumericCompare = aChar - bChar;
				} else if (aNumber)
					return 1;
				else if (bNumber)
					return -1;
				else if (lastNumericCompare == 0) {
					if (aChar != bChar)
						return aChar - bChar;
					asNumeric = false;
				} else
					return lastNumericCompare;
			else if (aNumber && bNumber) {
				asNumeric = true;
				if (lastNumericCompare == 0)
					lastNumericCompare = aChar - bChar;
			} else if (aChar != bChar)
				return aChar - bChar;
		}
		if (asNumeric)
			if (aLength > bLength && a.charAt(bLength) >= '0' && a.charAt(bLength) <= '9') // as number
				return 1; // a has bigger size, thus b is smaller
			else if (bLength > aLength && b.charAt(aLength) >= '0' && b.charAt(aLength) <= '9') // as number
				return -1; // b has bigger size, thus a is smaller
			else if (lastNumericCompare == 0)
				return aLength - bLength;
			else
				return lastNumericCompare;
		else
			return aLength - bLength;
	}

	public static boolean canPlayerBreak(Player player, BlockState state, BlockPos pos) {
		if (!player.mayBuild() || !player.level.mayInteract(player, pos)) {
			return false;
		}
		if (!player.isCreative() && state.getDestroyProgress(player, player.level, pos) <= 0) {
			return false;
		}
		BreakEvent event = new BreakEvent(player.level, pos, state, player);
		if (MinecraftForge.EVENT_BUS.post(event)) {
			return false;
		}
		return true;
	}

	public static int applyAlpha(int color, float alpha) {
		int prevAlphaChannel = (color >> 24) & 0xFF;
		if (prevAlphaChannel > 0)
			alpha *= prevAlphaChannel / 256f;
		int alphaChannel = (int) (0xFF * Mth.clamp(alpha, 0, 1));
		if (alphaChannel < 5) // fix font renderer bug
			return 0;
		return (color & 0xFFFFFF) | alphaChannel << 24;
	}

	public static float getPickRange(Player player) {
		float attrib = (float) player.getAttribute(ForgeMod.REACH_DISTANCE.get()).getValue();
		return player.isCreative() ? attrib : attrib - 0.5F;
	}

	public static void displayClientMessage(@Nullable Player player, boolean client, String key, Object... args) {
		if (player == null) {
			return;
		}
		if (client != player.level.isClientSide) {
			return;
		}
		player.displayClientMessage(new TranslatableComponent(key, args), true);
	}

	public static void jsonList(JsonElement json, Consumer<JsonElement> collector) {
		if (json.isJsonArray()) {
			for (JsonElement e : json.getAsJsonArray()) {
				collector.accept(e);
			}
		} else {
			collector.accept(json);
		}
	}

	@Nullable
	public static String[] readNBTStrings(CompoundTag tag, String key, @Nullable String[] strings) {
		if (!tag.contains(key, Tag.TAG_LIST)) {
			return null;
		}
		ListTag list = tag.getList(key, Tag.TAG_STRING);
		if (list.isEmpty()) {
			return null;
		}
		if (strings == null || strings.length != list.size()) {
			strings = new String[list.size()];
		}
		for (int i = 0; i < strings.length; i++) {
			String s = list.getString(i);
			strings[i] = s;
		}
		return strings;
	}

	public static void writeNBTStrings(CompoundTag tag, String key, @Nullable String[] strings) {
		if (strings == null || strings.length == 0) {
			return;
		}
		ListTag list = new ListTag();
		for (String s : strings) {
			list.add(StringTag.valueOf(s));
		}
		tag.put(key, list);
	}
}
