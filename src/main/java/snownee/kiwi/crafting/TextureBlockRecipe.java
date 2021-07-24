package snownee.kiwi.crafting;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;

import net.minecraft.core.NonNullList;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.ShapedRecipe;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.registries.ForgeRegistryEntry;
import snownee.kiwi.data.DataModule;
import snownee.kiwi.util.NBTHelper;
import snownee.kiwi.util.Util;

public class TextureBlockRecipe extends DynamicShapedRecipe {
	private final List<String> textureKeys;
	private final List<String> marks;
	private int keyCount = -1;

	public TextureBlockRecipe(ResourceLocation idIn, String groupIn, int recipeWidthIn, int recipeHeightIn, NonNullList<Ingredient> ingredients, ItemStack recipeOutputIn, List<String> textureKeys, List<String> marks) {
		super(idIn, groupIn, recipeWidthIn, recipeHeightIn, ingredients, recipeOutputIn);
		this.textureKeys = textureKeys;
		this.marks = marks;
	}

	@Override
	protected boolean checkMatch(CraftingContainer inv, int startX, int startY) {
		return checkMatchInternal(inv, startX, startY) != null;
	}

	private Map<String, ItemStack> checkMatchInternal(CraftingContainer inv, int startX, int startY) {
		Map<String, ItemStack> result = null;
		int i = 0;
		for (int y = startY; y < startY + getRecipeHeight(); ++y) {
			for (int x = startX; x < startX + getRecipeWidth(); ++x) {
				String key = textureKeys.get(i);
				if (key != null) {
					if (result == null) {
						if (keyCount < 0) {
							Set<String> set = Sets.newHashSet(textureKeys);
							set.remove(null);
							keyCount = set.size();
						}
						result = Maps.newHashMapWithExpectedSize(keyCount);
					}
					ItemStack slotStack = inv.getItem(x + y * inv.getWidth());
					Ingredient ingredient = getIngredients().get(x - startX + (y - startY) * getRecipeWidth());
					if (!(ingredient instanceof FullBlockIngredient) && !FullBlockIngredient.isTextureBlock(slotStack)) {
						return null;
					}
					ItemStack stack = result.getOrDefault(key, ItemStack.EMPTY);
					if (stack.isEmpty()) {
						result.put(key, slotStack);
					} else {
						if (!stack.sameItem(slotStack)) {
							return null;
						}
					}
				}
				if (!matches(inv, x, y, x - startX, y - startY)) {
					return null;
				}
				++i;
			}
		}
		return result;
	}

	@Override
	public ItemStack assemble(CraftingContainer inv) {
		int[] pos = getMatchPos(inv);
		if (pos == null) {
			return ItemStack.EMPTY;
		}
		Map<String, ItemStack> result = checkMatchInternal(inv, pos[0], pos[1]);
		if (result == null) {
			return ItemStack.EMPTY;
		}
		ItemStack stack = getResultItem().copy();
		NBTHelper data = NBTHelper.of(stack.getOrCreateTagElement("BlockEntityTag"));
		for (Entry<String, ItemStack> e : result.entrySet()) {
			Item item = e.getValue().getItem();
			if (item instanceof BlockItem) {
				BlockState state = ((BlockItem) item).getBlock().defaultBlockState();
				for (String k : e.getKey().split(",")) {
					String texture = NBTHelper.of(e.getValue()).getString("BlockEntityTag.Textures." + k);
					if (texture == null) {
						texture = NbtUtils.writeBlockState(state).toString();
					}
					data.setString("Textures." + k, texture);
					if (marks.contains(k)) {
						data.setString("Items." + k, Util.trimRL(item.getRegistryName()));
					}
				}
			} else {
				return ItemStack.EMPTY;
			}
		}
		return stack;
	}

	@Override
	public RecipeSerializer<?> getSerializer() {
		return DataModule.TEXTURE_BLOCK;
	}

	public static class Serializer extends ForgeRegistryEntry<RecipeSerializer<?>> implements RecipeSerializer<TextureBlockRecipe> {
		@Override
		public TextureBlockRecipe fromJson(ResourceLocation recipeId, JsonObject json) {
			String group = GsonHelper.getAsString(json, "group", "");
			Map<String, Ingredient> ingredientMap = ShapedRecipe.keyFromJson(GsonHelper.getAsJsonObject(json, "key"));
			String[] pattern = ShapedRecipe.shrink(ShapedRecipe.patternFromJson(GsonHelper.getAsJsonArray(json, "pattern")));
			int width = pattern[0].length();
			int height = pattern.length;
			NonNullList<Ingredient> nonnulllist = ShapedRecipe.dissolvePattern(pattern, ingredientMap, width, height);
			ItemStack itemstack = ShapedRecipe.itemStackFromJson(GsonHelper.getAsJsonObject(json, "result"));

			Map<String, String> texMap = Maps.newHashMap();
			for (Entry<String, JsonElement> entry : GsonHelper.getAsJsonObject(json, "texture").entrySet()) {
				if (entry.getKey().length() != 1) {
					throw new JsonSyntaxException("Invalid key entry: '" + entry.getKey() + "' is an invalid symbol (must be 1 character only).");
				}
				if (" ".equals(entry.getKey()) || ",".equals(entry.getKey())) {
					throw new JsonSyntaxException("Invalid key entry: '" + entry.getKey() + "' is a reserved symbol.");
				}
				if (entry.getValue().isJsonArray()) {
					/* off */
                    List<String> keys = Lists.newArrayList(entry.getValue().getAsJsonArray())
                            .stream()
                            .map(JsonElement::getAsString)
                            .collect(Collectors.toList());
                    /* on */
					texMap.put(entry.getKey(), StringUtils.join(keys, ','));
				} else {
					texMap.put(entry.getKey(), entry.getValue().getAsString());
				}
			}
			List<String> keys = Lists.newArrayListWithExpectedSize(width * height);
			Set<String> set = Sets.newHashSet(texMap.keySet());
			for (String element : pattern) {
				for (int j = 0; j < element.length(); ++j) {
					String s = element.substring(j, j + 1);
					if (texMap.containsKey(s)) {
						keys.add(texMap.get(s));
						set.remove(s);
					} else {
						keys.add(null);
					}
				}
			}
			if (!set.isEmpty()) {
				throw new JsonSyntaxException("Key defines symbols that aren't used in pattern: " + set);
			}
			List<String> marks;
			if (GsonHelper.isArrayNode(json, "mark")) {
				JsonArray array = GsonHelper.getAsJsonArray(json, "mark");
				marks = Lists.newArrayListWithCapacity(array.size());
				array.forEach(e -> marks.add(e.getAsString()));
			} else {
				String mark = GsonHelper.getAsString(json, "mark", "");
				marks = Collections.singletonList(mark);
			}
			return new TextureBlockRecipe(recipeId, group, width, height, nonnulllist, itemstack, keys, marks);
		}

		@Override
		public TextureBlockRecipe fromNetwork(ResourceLocation recipeId, FriendlyByteBuf buffer) {
			int width = buffer.readVarInt();
			int height = buffer.readVarInt();
			String s = buffer.readUtf(256);
			NonNullList<Ingredient> nonnulllist = NonNullList.withSize(width * height, Ingredient.EMPTY);
			for (int k = 0; k < nonnulllist.size(); ++k) {
				nonnulllist.set(k, Ingredient.fromNetwork(buffer));
			}
			ItemStack itemstack = buffer.readItem();

			List<String> keys = Lists.newArrayListWithExpectedSize(width * height);
			for (int i = 0; i < width * height; i++) {
				String k = buffer.readUtf(16);
				keys.add(k.isEmpty() ? null : k);
			}
			List<String> marks = ImmutableList.copyOf(buffer.readUtf(256).split(","));
			return new TextureBlockRecipe(recipeId, s, width, height, nonnulllist, itemstack, keys, marks);
		}

		@Override
		public void toNetwork(FriendlyByteBuf buffer, TextureBlockRecipe recipe) {
			buffer.writeVarInt(recipe.getRecipeWidth());
			buffer.writeVarInt(recipe.getRecipeHeight());
			buffer.writeUtf(recipe.getGroup());
			for (Ingredient ingredient : recipe.getIngredients()) {
				ingredient.toNetwork(buffer);
			}
			buffer.writeItem(recipe.getResultItem());

			for (int i = 0; i < recipe.getRecipeWidth() * recipe.getRecipeHeight(); i++) {
				String k = recipe.textureKeys.get(i);
				buffer.writeUtf(k == null ? "" : k);
			}
			buffer.writeUtf(StringUtils.join(recipe.marks), ',');
		}
	}
}
