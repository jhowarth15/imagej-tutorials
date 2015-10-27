/*
 * To the extent possible under law, the ImageJ developers have waived
 * all copyright and related or neighboring rights to this tutorial code.
 *
 * See the CC0 1.0 Universal license for details:
 *     http://creativecommons.org/publicdomain/zero/1.0/
 */

import net.imagej.ImageJ;
import net.imagej.ops.ComputerOp;
import net.imagej.ops.FunctionOp;
import net.imagej.ops.HybridOp;
import net.imagej.ops.IIs;
import net.imagej.ops.InplaceOp;
import net.imagej.ops.Ops;
import net.imagej.ops.RAIs;
import net.imagej.ops.RTs;
import net.imglib2.IterableInterval;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.img.array.ArrayImgs;
import net.imglib2.type.numeric.integer.ByteType;
import net.imglib2.type.numeric.real.DoubleType;

/** How to use ImageJ Operations. */
public class UsingOpTypes {
	public static void main(final String... args) throws Exception {
		ImageJ ij = new ImageJ();
		IterableInterval<DoubleType> image = ArrayImgs.doubles(256, 256);
		IterableInterval<DoubleType> output = ArrayImgs.doubles(256, 256);
		DoubleType in = new DoubleType(10.0);
		DoubleType out = new DoubleType();

		ij.log().info("--------- Computer op: Stores results in a output reference ---------"); 
		ComputerOp<DoubleType, DoubleType> add5 = ij.op().computer(Ops.Math.Add.class, DoubleType.class, DoubleType.class, 5.0);
		add5.compute(in, out);
		ij.log().info("--------- Out = " + out + " ---------\n"); 

		ij.log().info("--------- Call helper class if using generics as I/O to get around raw types ---------");
		ij.log().info("--------- Without calling helper class, you get unsafe raw types ---------");
		ComputerOp<IterableInterval, RandomAccessibleInterval> badAdd = ij.op().computer(Ops.Math.Add.class, RandomAccessibleInterval.class, IterableInterval.class, in);
		@SuppressWarnings("unused")
		RandomAccessibleInterval<ByteType> byteImage = ArrayImgs.bytes(256,256);
		//Uncomment and run: Will cause a runtime error, bad!
		//badAdd.compute(image, byteImage); 
		ij.log().info("--------- There are currently three type safe helper classes ---------");
		ij.log().info("--------- RAIs, IIs, and RTs for when RandomAccessibleInterval, IterableInterval, and RealType is the output, respectfully ---------\n");
		@SuppressWarnings("unused")
		ComputerOp<IterableInterval<DoubleType>, RandomAccessibleInterval<DoubleType>> goodAdd = RAIs.computer(ij.op(), Ops.Math.Add.class, image, in);
		//Uncomment: Will get a compiler error instead, better
		//goodAdd.compute(image, byteImage);
		ComputerOp<String, IterableInterval<DoubleType>> equation = IIs.computer(ij.op(), Ops.Image.Equation.class, "p[0]+p[1]");	
		equation.compute("p[0]+p[1]", image);
		ij.ui().show("image", image);

		ij.log().info("--------- Function op: Returns the result as a new object ---------"); 
		FunctionOp<IterableInterval<DoubleType>, DoubleType> functionMean = RTs.function(ij.op(), Ops.Stats.Mean.class, image);
		DoubleType m = functionMean.compute(image);
		ij.log().info("--------- Stats: Mean = " + m + " ---------\n"); 

		ij.log().info("--------- Inplace op: mutate the given input ---------"); 
		ij.log().info("--------- Loop op: Execute op on the input for a certain number of times --------");
		int iterations = 4;
		InplaceOp<DoubleType> addLoop = RTs.inplace(ij.op(), Ops.Loop.class, in, add5, iterations);
		addLoop.compute(in);	
		ij.log().info("--------- 'In' is modified from 10.0 to " + in + " ---------\n"); 

		ij.log().info("--------- Hybrid op: Can be used either as function or computer op ---------"); 
		HybridOp<IterableInterval<DoubleType>, DoubleType> meanOp = RTs.hybrid(ij.op(), Ops.Stats.Mean.class, image);
		HybridOp<IterableInterval<DoubleType>, DoubleType> maxOp = RTs.hybrid(ij.op(), Ops.Stats.Max.class, image);		
		DoubleType mean = new DoubleType(0);
		meanOp.compute(image, mean); //As computer
		DoubleType max = maxOp.compute(image); //As function
		ij.log().info("--------- Stats: Mean = " + mean + " Max = " + max + " ---------\n"); 

		ij.log().info("-------- Map op: Execute op on every pixel of an image --------");
		ComputerOp<IterableInterval<DoubleType>, IterableInterval<DoubleType>> mapOp = IIs.computer(ij.op(), Ops.Map.class, image, add5);
		mapOp.compute(image, output);
		ij.log().info("-------- Input mean: " + meanOp.compute(image) + " Output mean: " + meanOp.compute(output) + " --------\n");
		ij.ui().show("Map output", output);

		ij.log().info("-------- Join op: Execute op on every pixel of an image --------\n");

		ij.log().info("--------- Searching for the op is slower: ---------");
		long start = System.currentTimeMillis();
		for (int i = 0; i < 1000; i++) {			
			ij.op().math().add(out, in, 5);  //Searches for the op everytime and then runs it
		}
		long mid = System.currentTimeMillis();
		for (int i = 0; i < 1000; i++) {
			add5.compute(in, out);	//Just runs the op
		}
		long end = System.currentTimeMillis();
		ij.log().info("--------- Slow = " + (mid - start) + "ms ---------");
		ij.log().info("--------- Fast = " + (end - mid) + "ms ---------\n");

		ij.log().info("--------- All done! ---------");
	}
}
