/*
 * To the extent possible under law, the ImageJ developers have waived
 * all copyright and related or neighboring rights to this tutorial code.
 *
 * See the CC0 1.0 Universal license for details:
 *     http://creativecommons.org/publicdomain/zero/1.0/
 */

import net.imagej.ImageJ;
import net.imglib2.Point;
import net.imglib2.algorithm.region.hypersphere.HyperSphere;
import net.imglib2.img.Img;
import net.imglib2.img.array.ArrayImgFactory;
import net.imglib2.type.numeric.real.FloatType;

/** How to use ImageJ Ops Convolution */
public class ConvolutionOps {

	public static void main(final String... args) throws Exception {
		final ImageJ ij = new ImageJ();

		int numDimensions = 2;

		int[] size = new int[] { 200, 200 };

		// create an input with a small sphere at the center
		Img<FloatType> in = new ArrayImgFactory<FloatType>().create(size,
				new FloatType());
		placeSphereInCenter(in);

		// show the image in a window
		ij.ui().show("input", in);

		double sigma = 5.0;

		// create a Gaussian Kernel
		Img<FloatType> kernel = (Img<FloatType>) ij.op().run("gausskernel",
				new FloatType(), new ArrayImgFactory(), numDimensions, sigma,
				null);

		// show the image in a window
		ij.ui().show("kernel", kernel);

		// convolve
		Img<FloatType> out = (Img<FloatType>) ij.op().run("convolve", in,
				kernel);

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
