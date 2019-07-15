package snownee.kiwi;

import java.util.Map;
import java.util.function.BiConsumer;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.item.Item;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLDedicatedServerSetupEvent;
import net.minecraftforge.fml.event.server.FMLServerStartingEvent;
import net.minecraftforge.registries.IForgeRegistryEntry;
import snownee.kiwi.block.ModBlock;

/**
 * 
 * All your modules should extend {@code AbstractModule}
 * 
 * @author Snownee
 *
 */
public abstract class AbstractModule
{
    private static final BiConsumer<ModuleInfo, IForgeRegistryEntry<?>> ITEM_DECORATOR = (module, entry) -> {
        Item item = (Item) entry;
        if (module.group != null && item.group == null && !module.noGroups.contains(item))
            item.group = module.group;
    };

    private static final Map<Class, BiConsumer<ModuleInfo, IForgeRegistryEntry<?>>> DEFAULT_DECORATORS = ImmutableMap.of(Item.class, ITEM_DECORATOR);

    protected final Map<Class, BiConsumer<ModuleInfo, IForgeRegistryEntry<?>>> decorators = Maps.newHashMap(DEFAULT_DECORATORS);

    protected void preInit()
    {
        // NO-OP
    }

    /**
     * @author Snownee
     * @param event Note: this event's ModContainer is from Kiwi
     */
    protected void init(FMLCommonSetupEvent event)
    {
        // NO-OP
    }

    protected void clientInit(FMLClientSetupEvent event)
    {
        // NO-OP
    }

    @Deprecated
    protected void serverInit(FMLDedicatedServerSetupEvent event)
    {
        // NO-OP
    }

    protected void serverInit(FMLServerStartingEvent event)
    {
        // NO-OP
    }

    protected void postInit()
    {
        // NO-OP
    }

    /// helper methods:
    protected static Item.Properties itemProp()
    {
        return new Item.Properties();
    }

    protected static Block.Properties blockProp(Material material)
    {
        return Block.Properties.create(material);
    }

    protected static <T extends Block> T init(T block)
    {
        return ModBlock.deduceSoundAndHardness(block);
    }
}
