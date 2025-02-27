package snownee.kiwi.mixin;

import java.util.Set;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.gen.Accessor;

import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.item.Item;

@Mixin(Villager.class)
public interface VillagerAccess {

	@Accessor
	static Set<Item> getWANTED_ITEMS() {
		throw new IllegalStateException();
	}

	@Accessor
	@Final
	@Mutable
	static void setWANTED_ITEMS(Set<Item> set) {
		throw new IllegalStateException();
	}

}
