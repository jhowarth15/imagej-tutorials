/*
 * To the extent possible under law, the ImageJ developers have waived
 * all copyright and related or neighboring rights to this tutorial code.
 *
 * See the CC0 1.0 Universal license for details:
 *     http://creativecommons.org/publicdomain/zero/1.0/
 */

import net.imagej.Dataset;
import net.imagej.ImageJ;
import net.imagej.ops.OpService;
import net.imagej.ops.Ops;

import org.scijava.ItemIO;
import org.scijava.ItemVisibility;
import org.scijava.app.StatusService;
import org.scijava.command.Command;
import org.scijava.log.LogService;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;

/** Adds two datasets using the ImageJ Ops framework. */
@Plugin(type = Command.class, headless = true,
	menuPath = "Tutorials>Add Two Datasets")
public class AddTwoDatasets implements Command {

	@Parameter
	private LogService log;

	@Parameter
	private StatusService statusService;

	@Parameter
	private OpService ops;

	@Parameter(visibility = ItemVisibility.MESSAGE)
	private final String header = "This demonstration adds two datasets";

	@Parameter(label = "First dataset")
	private Dataset dataset1;

	@Parameter(label = "Second dataset")
	private Dataset dataset2;

	@Parameter(type = ItemIO.OUTPUT)
	private Object result;

	public static void main(final String... args) throws Exception {
		// create the ImageJ application context with all available services
		final ImageJ ij = net.imagej.Main.launch(args);

		ij.command().run(OpenImage.class, true);

		ij.command().run(AddTwoDatasets.class, true);
	}

	@Override
	public void run() {
		// verify images are compatible
		if (dataset1.numDimensions() != dataset2.numDimensions()) {
			log.error("Input datasets must have the same number of dimensions.");
			return;
		}

		// add them together
		result = ops.run(Ops.Math.Add.class, dataset1, dataset2);
	}

}
