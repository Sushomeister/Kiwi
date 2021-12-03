package snownee.kiwi.data;

import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraftforge.common.crafting.CraftingHelper;
import snownee.kiwi.AbstractModule;
import snownee.kiwi.KiwiModule;
import snownee.kiwi.loader.event.InitEvent;
import snownee.kiwi.recipe.AlternativesIngredientSerializer;
import snownee.kiwi.recipe.FullBlockIngredient;
import snownee.kiwi.recipe.ModuleLoadedCondition;
import snownee.kiwi.recipe.TryParseCondition;
import snownee.kiwi.recipe.crafting.NoContainersShapedRecipe;
import snownee.kiwi.recipe.crafting.NoContainersShapelessRecipe;
import snownee.kiwi.recipe.crafting.RetextureRecipe;

@KiwiModule("data")
public final class DataModule extends AbstractModule {

	public static final RecipeSerializer<NoContainersShapedRecipe> SHAPED_NO_CONTAINERS = new NoContainersShapedRecipe.Serializer();
	public static final RecipeSerializer<NoContainersShapelessRecipe> SHAPELESS_NO_CONTAINERS = new NoContainersShapelessRecipe.Serializer();
	public static final RecipeSerializer<RetextureRecipe> RETEXTURE = new RetextureRecipe.Serializer();

	@Override
	protected void init(InitEvent event) {
		CraftingHelper.register(ModuleLoadedCondition.Serializer.INSTANCE);
		CraftingHelper.register(TryParseCondition.Serializer.INSTANCE);
		CraftingHelper.register(RL("full_block"), FullBlockIngredient.SERIALIZER);
		CraftingHelper.register(RL("alternatives"), AlternativesIngredientSerializer.INSTANCE);
	}
}
