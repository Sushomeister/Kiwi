/*
The MIT License (MIT)

Copyright (c) 2014-2015 mezz

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in
all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
THE SOFTWARE.
 */

package snownee.kiwi.client;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Stream;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.client.resources.TextureAtlasHolder;
import net.minecraft.resources.ResourceLocation;
import snownee.kiwi.Kiwi;

public class KiwiTextureManager extends TextureAtlasHolder {
	public static final ResourceLocation LOCATION_GUI_TEXTURE = new ResourceLocation(Kiwi.MODID, "textures/atlas/gui.png");
	public static final KiwiTextureManager GUI_ATLAS = new KiwiTextureManager(Minecraft.getInstance().textureManager, LOCATION_GUI_TEXTURE);

	private final Set<ResourceLocation> registeredSprites = new HashSet<>();
	private final ResourceLocation atlasLocation;

	public KiwiTextureManager(TextureManager textureManager, ResourceLocation atlasLocation, String prefixIn) {
		super(textureManager, atlasLocation, prefixIn);
		this.atlasLocation = atlasLocation;
	}

	public KiwiTextureManager(TextureManager textureManager, ResourceLocation atlasLocation) {
		this(textureManager, atlasLocation, "gui");
	}

	public void registerSprite(ResourceLocation location) {
		registeredSprites.add(location);
	}

	@Override
	public Stream<ResourceLocation> getResourcesToLoad() {
		return registeredSprites.stream();
	}

	/**
	 * Overridden to make it public
	 */
	@Override
	public TextureAtlasSprite getSprite(ResourceLocation location) {
		return super.getSprite(location);
	}

	public ResourceLocation getLocation() {
		return atlasLocation;
	}

}
