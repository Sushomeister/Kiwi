package snownee.kiwi.util;

import java.util.Collection;
import java.util.List;

import com.google.common.collect.Lists;
import com.mojang.math.Vector3f;

import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;

public final class MathUtil {
	private MathUtil() {
	}

	/*
     * https://stackoverflow.com/questions/9600801/evenly-distributing-n-points-on-a-sphere/26127012#26127012
     */
	public static List<Vec3> fibonacciSphere(Vec3 start, double radius, int samples, boolean randomize) {
		double rnd = 1;
		if (randomize)
			rnd = Math.random() * samples;
		double offset = 2d / samples;
		double increment = Math.PI * (3 - Math.sqrt(5));
		List<Vec3> points = Lists.newArrayListWithCapacity(samples);
		for (int i = 0; i < samples; i++) {
			double y = ((i * offset) - 1) + (offset / 2);
			double r = Math.sqrt(1 - y * y) * radius;
			double phi = ((i + rnd) % samples) * increment;
			double x = Math.cos(phi) * r;
			double z = Math.sin(phi) * r;
			points.add(new Vec3(start.x + x, start.y + y * radius, start.z + z));
		}
		return points;
	}

	public static int posOnLine(Vec3 start, Vec3 end, Collection<BlockPos> list) {
		list.add(new BlockPos(start));
		if (start.equals(end)) {
			return 1;
		} else {
			int c = 1;
			double ex = Mth.lerp(-1.0E-7D, end.x, start.x);
			double ey = Mth.lerp(-1.0E-7D, end.y, start.y);
			double ez = Mth.lerp(-1.0E-7D, end.z, start.z);
			double sx = Mth.lerp(-1.0E-7D, start.x, end.x);
			double sy = Mth.lerp(-1.0E-7D, start.y, end.y);
			double sz = Mth.lerp(-1.0E-7D, start.z, end.z);
			int x = Mth.floor(sx);
			int y = Mth.floor(sy);
			int z = Mth.floor(sz);

			double subX = ex - sx;
			double subY = ey - sy;
			double subZ = ez - sz;
			int signX = Mth.sign(subX);
			int signY = Mth.sign(subY);
			int signZ = Mth.sign(subZ);
			double d9 = signX == 0 ? Double.MAX_VALUE : signX / subX;
			double d10 = signY == 0 ? Double.MAX_VALUE : signY / subY;
			double d11 = signZ == 0 ? Double.MAX_VALUE : signZ / subZ;
			double d12 = d9 * (signX > 0 ? 1.0D - Mth.frac(sx) : Mth.frac(sx));
			double d13 = d10 * (signY > 0 ? 1.0D - Mth.frac(sy) : Mth.frac(sy));
			double d14 = d11 * (signZ > 0 ? 1.0D - Mth.frac(sz) : Mth.frac(sz));

			while (d12 <= 1.0D || d13 <= 1.0D || d14 <= 1.0D) {
				if (d12 < d13) {
					if (d12 < d14) {
						x += signX;
						d12 += d9;
					} else {
						z += signZ;
						d14 += d11;
					}
				} else if (d13 < d14) {
					y += signY;
					d13 += d10;
				} else {
					z += signZ;
					d14 += d11;
				}

				list.add(new BlockPos(x, y, z));
				++c;
			}

			return c;
		}
	}

	/**
	 * HSV to RGB: Mth
	 * @since 2.7.0
	 */
	public static Vector3f RGBtoHSV(int rgb) {
		int r = (rgb >> 16) & 255;
		int g = (rgb >> 8) & 255;
		int b = rgb & 255;
		int max = Math.max(r, Math.max(g, b));
		int min = Math.min(r, Math.min(g, b));
		float v = max;
		float delta = max - min;
		float h, s;
		if (max != 0)
			s = delta / max; // s
		else {
			// r = g = b = 0        // s = 0, v is undefined
			s = 0;
			h = -1;
			return new Vector3f(h, s, 0 /*Float.NaN*/);
		}
		if (r == max)
			h = (g - b) / delta; // between yellow & magenta
		else if (g == max)
			h = 2 + (b - r) / delta; // between cyan & yellow
		else
			h = 4 + (r - g) / delta; // between magenta & cyan
		h /= 6; // degrees
		if (h < 0)
			h += 1;
		return new Vector3f(h, s, v / 255);
	}
}
