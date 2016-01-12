/*
 * To the extent possible under law, the ImageJ developers have waived
 * all copyright and related or neighboring rights to this tutorial code.
 *
 * See the CC0 1.0 Universal license for details:
 *     http://creativecommons.org/publicdomain/zero/1.0/
 */

import org.scijava.log.LogService;

import net.imagej.ImageJ;
import net.imagej.ops.Ops;
import net.imagej.ops.chain.IIs;
import net.imagej.ops.chain.RTs;
import net.imagej.ops.special.Computers;
import net.imagej.ops.special.InplaceOp;
import net.imagej.ops.special.UnaryComputerOp;
import net.imagej.ops.special.UnaryFunctionOp;
import net.imagej.ops.special.UnaryHybridOp;
import net.imglib2.IterableInterval;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.img.array.ArrayImgs;
import net.imglib2.type.numeric.integer.ByteType;
import net.imglib2.type.numeric.real.DoubleType;

/** How to use ImageJ Operations. */
public class UsingSpecialOps {
	@SuppressWarnings("unused")
	public static void main(final String... args) throws Exception {
		ImageJ ij = new ImageJ();
		LogService log = ij.log();
		IterableInterval<DoubleType> image = ArrayImgs.doubles(256, 256); //creates a blank iterableInterval
		IterableInterval<DoubleType> output = ArrayImgs.doubles(256, 256);
		DoubleType in = new DoubleType(10.0);
		DoubleType out = new DoubleType();

		log.info("--------- Computer op: Stores results in a output reference ---------"); 
		UnaryComputerOp<DoubleType, DoubleType> add5 = Computers.unary(ij.op(), Ops.Math.Add.class, DoubleType.class, DoubleType.class, 5.0);
		add5.compute1(in, out); //Add 5 to 'in' and stores result in 'out'
		log.info("--------- Out = " + out + " ---------\n");

		log.info("--------- Call helper class to get around raw types if using generics as I/O  ---------");
		log.info("--------- Without calling helper class, you get unsafe raw types ---------");
		@SuppressWarnings("rawtypes")
		UnaryComputerOp<IterableInterval, RandomAccessibleInterval> badAdd = Computers.unary(ij.op(), Ops.Math.Add.class, RandomAccessibleInterval.class, IterableInterval.class, in);
		RandomAccessibleInterval<ByteType> byteImage = ArrayImgs.bytes(256,256);
		//Uncomment and run: Will cause a runtime error, bad!
		//badAdd.compute(image, byteImage);
		log.info("--------- There are currently three type safe helper classes ---------");
		log.info("--------- RAIs, IIs, and RTs for when RandomAccessibleInterval, IterableInterval, and RealType is the output, respectfully ---------\n");
		UnaryComputerOp<IterableInterval<DoubleType>, IterableInterval<DoubleType>> goodAdd = IIs.computer(ij.op(), Ops.Math.Add.class, image, in);
		//Uncomment: Will get a compiler error instead, better
		//goodAdd.compute(image, byteImage);
		
		UnaryComputerOp<String, IterableInterval<DoubleType>> equation = Computers.unary(ij.op(), Ops.Image.Equation.class, image, "p[0]+p[1]");
		equation.compute1("p[0]+p[1]", image); //Applies the equation to the 'image'
		ij.ui().show("Image", image); //Shows the 'image' in a window

		log.info("--------- Function op: Returns the result as a new object ---------");
		UnaryFunctionOp<IterableInterval<DoubleType>, DoubleType> functionMean = RTs.function(ij.op(), Ops.Stats.Mean.class, image);
		DoubleType m = functionMean.compute1(image);  //Computes the mean value of the iterable and returns it as a new object
		log.info("--------- Stats: Mean = " + m + " ---------\n");

		log.info("--------- Inplace op: mutate the given input ---------");
		log.info("--------- Loop op: Execute op on the input for a certain number of times --------");
		int iterations = 4;
		InplaceOp<DoubleType> addLoop = RTs.inplace(ij.op(), Ops.Loop.class, in, add5, iterations);
		addLoop.mutate(in); //Adds 5 to 'in' each time through the loop.  Stores value in 'in'.
		log.info("--------- 'In' is modified from 10.0 to " + in + " ---------\n"); 

		log.info("--------- Hybrid op: Can be used either as a function or computer op ---------"); 
		UnaryHybridOp<IterableInterval<DoubleType>, DoubleType> meanOp = RTs.hybrid(ij.op(), Ops.Stats.Mean.class, image);
		UnaryHybridOp<IterableInterval<DoubleType>, DoubleType> maxOp = RTs.hybrid(ij.op(), Ops.Stats.Max.class, image);		
		DoubleType mean = new DoubleType(0);
		meanOp.compute1(image, mean); //Uses the HybridOp as a ComputerOp (stores result in output reference)
		DoubleType max = maxOp.compute1(image); //Uses the HybridOp as a FunctionOp (returns result as a new object)
		log.info("--------- Stats: Mean = " + mean + " Max = " + max + " ---------\n"); 

		log.info("-------- Map op: Execute op on every pixel of an image --------");
		UnaryComputerOp<IterableInterval<DoubleType>, IterableInterval<DoubleType>> mapOp = IIs.computer(ij.op(), Ops.Map.class, image, add5);
		mapOp.compute1(image, output); //Adds 5 to each pixel, and returns the output as the given output reference
		log.info("-------- Input mean: " + meanOp.compute1(image) + " Output mean: " + meanOp.compute1(output) + " --------\n");
		ij.ui().show("Map output", output); 
		
		log.info("--------- Searching for the op is slower: ---------");
		long start = System.currentTimeMillis();
		for (int i = 0; i < 1000; i++) {			
			ij.op().math().add(out, in, 5);  //Searches for the op everytime and then runs it
		}
		long mid = System.currentTimeMillis();
		for (int i = 0; i < 1000; i++) {
			add5.compute1(in, out);	//Just runs the op
		}
		long end = System.currentTimeMillis();
		log.info("--------- Slow = " + (mid - start) + "ms ---------");
		log.info("--------- Fast = " + (end - mid) + "ms ---------\n");

		log.info("--------- All done! ---------");
	}
}
