/*
 * To the extent possible under law, the ImageJ developers have waived
 * all copyright and related or neighboring rights to this tutorial code.
 *
 * See the CC0 1.0 Universal license for details:
 *     http://creativecommons.org/publicdomain/zero/1.0/
 */

import net.imagej.ImageJ;
import net.imglib2.Point;
import net.imglib2.RandomAccess;
import net.imglib2.RandomAccessible;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.algorithm.region.hypersphere.HyperSphere;
import net.imglib2.img.Img;
import net.imglib2.img.array.ArrayImgFactory;
import net.imglib2.outofbounds.OutOfBoundsConstantValueFactory;
import net.imglib2.outofbounds.OutOfBoundsFactory;
import net.imglib2.type.numeric.real.FloatType;
import net.imglib2.util.Util;
import net.imglib2.view.Views;
import net.imagej.ops.convolve.ConvolveNaive;

/** How to use ImageJ Ops Convolution */
public class ConvolutionOps {

	public static void main(final String... args) throws Exception {
		final ImageJ ij = new ImageJ();

		int[] size = new int[] { 200, 200 };

		// create an input with a small sphere at the center
		Img<FloatType> in = new ArrayImgFactory<FloatType>().create(size,
				new FloatType());
		placeSphereInCenter(in);

		// show the image in a window
		ij.ui().show("input", in);

		int[] kernelSize = new int[] { 3, 3 };
		Img<FloatType> kernel = new ArrayImgFactory<FloatType>().create(
				kernelSize, new FloatType());
		RandomAccess<FloatType> kernelRa = kernel.randomAccess();
		// long[] borderSize = new long[] {1, 1};
		int[] k = new int[] { 1, -2, 1, 2, -4, 2, 1, -2, 1 }; // Second
																// derivative
																// filter
		int h = 0;
		for (int i = 0; i < 3; i++) {
			for (int j = 0; j < 3; j++) {
				kernelRa.setPosition(new Point(i, j));
				kernelRa.get().set(k[h]);
				h++;
			}
		}

		// show the image in a window
		ij.ui().show("kernel", kernel);

		Img<FloatType> out = new ArrayImgFactory<FloatType>().create(in,
				new FloatType());

		OutOfBoundsFactory<FloatType, RandomAccessibleInterval<FloatType>> obf = new OutOfBoundsConstantValueFactory<FloatType, RandomAccessibleInterval<FloatType>>(
				Util.getTypeFromInterval(in).createVariable());

		// extend the input
		RandomAccessibleInterval<FloatType> extendedIn = Views.interval(
				Views.extend(in, obf), in);
		
		// extend the output
		RandomAccessibleInterval<FloatType> extendedOut = Views.interval(
				Views.extend(out, obf), out);

		// convolve
		ij.op().run(ConvolveNaive.class, extendedOut, extendedIn, kernel);

		// show the image in a window
		ij.ui().show("convolved", out);

	}

	// utility to place a small sphere at the center of the image
	private static void placeSphereInCenter(Img<FloatType> img) {

		final Point center = new Point(img.numDimensions());

		for (int d = 0; d < img.numDimensions(); d++)
			center.setPosition(img.dimension(d) / 2, d);

		HyperSphere<FloatType> hyperSphere = new HyperSphere<FloatType>(img,
				center, 10);

		for (final FloatType value : hyperSphere) {
			value.setReal(1);
		}
	}

}
