/*
 * To the extent possible under law, the ImageJ developers have waived
 * all copyright and related or neighboring rights to this tutorial code.
 *
 * See the CC0 1.0 Universal license for details:
 *     http://creativecommons.org/publicdomain/zero/1.0/
 */

import java.io.File;
import java.io.IOException;

import net.imagej.Dataset;
import net.imagej.ImageJ;

import org.scijava.ItemIO;
import org.scijava.command.Command;
import org.scijava.log.LogService;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;

import io.scif.services.DatasetIOService;

import ij.IJ;
import ij.ImagePlus;
import ij.io.OpenDialog;
import ij.plugin.PlugIn;
import java.io.IOException;
//import loci.formats.FormatException;
//import loci.plugins.BF;
import ij.plugin.FolderOpener;
import ij.ImageStack;
import ij.WindowManager;
import ij.gui.GenericDialog;
import ij.gui.ImageWindow;
import ij.gui.Roi;
import ij.gui.Toolbar;
import ij.io.Opener;
import ij.measure.Calibration;
import ij.measure.Measurements;
import ij.plugin.filter.Analyzer;
import ij.plugin.frame.PlugInFrame;
import ij.process.Blitter;
import ij.process.ByteProcessor;
import ij.process.ColorProcessor;
import ij.process.FloatProcessor;
import ij.process.ImageProcessor;
import ij.process.ImageStatistics;
import ij.process.ShortProcessor;
import ij.process.TypeConverter;

import java.awt.Button;
import java.awt.FileDialog;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.Label;
import java.awt.Panel;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;

/**
 * This tutorial shows how to use ImageJ services to open an image and display
 * it to the user.
 * <p>
 * A main method is provided so this class can be run directly from Eclipse (or
 * any other IDE).
 * </p>
 * <p>
 * Because this class implements {@link Command} and is annotated with
 * {@code @Plugin}, it will show up in the ImageJ menus: under Tutorials &gt;
 * Open Image, as specified by the {@code menuPath} field of the {@code @Plugin}
 * annotation.
 * </p>
 * <p>
 * See also the {@code LoadAndDisplayDataset} tutorial.
 * </p>
 */


@Plugin(type = Command.class, menuPath = "Josh>Open Image")
public class OpenImage extends PlugInFrame implements Command, ActionListener, Measurements {

	OpenImage oi;
	Panel panel;
	static Frame instance;
	ImagePlus imp = null;
	ImageStack stack = null;
	int numSlices,slice;
	protected Label label1;
	boolean doScaling = true;
	
	public void run(String arg) {
		oi = new OpenImage();
	}
	
	/**
	 * 
	 */
	public OpenImage() {
		super("Open Image");
		// TODO Auto-generated constructor stub
		//OpenImage instance = this;
		setLayout(new FlowLayout(FlowLayout.CENTER,10,10));
		panel = new Panel();
		panel.setLayout(new GridLayout(18,1,5,5));

		addButton("<-", panel);
		addButton("->", panel);
		add(panel);
		pack();
 		show();
 			
 		//IJ.showMessage("Select folder of PNG image files");
	
		FolderOpener fo = new FolderOpener( );
		fo.openAsVirtualStack(true);
		fo.run( null );
		

 		
	}

	//public ImagePlus openFolder(java.lang.String path);

	/*
	 * This first @Parameter is a core ImageJ service (and thus @Plugin). The
	 * context will provide it automatically when this command is created.
	 */

//	@Parameter
//	private DatasetIOService datasetIOService;

	/*
	 * In this command, we will be using functions that can throw exceptions.
	 * Best practice is to log these exceptions to let the user know what went
	 * wrong. By using the LogService to do this, we let the framework decide
	 * the best place to display thrown errors.
	 */
//	@Parameter
//	private LogService logService;

	/*
	 * We need to know what image to open. So, the framework will ask the user
	 * via the active user interface to select a file to open. This command is
	 * "UI agnostic": it does not need to know the specific user interface
	 * currently active.
	 */
//	@Parameter
//	private File imageFile;

	/*
	 * This command will produce an image that will automatically be shown by
	 * the framework. Again, this command is "UI agnostic": how the image is
	 * shown is not specified here.
	 */
//	@Parameter(type = ItemIO.OUTPUT)
//	private Dataset image;

/////////////////CLASS RUN FUNCTION///////////////////
	
	/*
	 * The run() method is where we do the actual 'work' of the command. In this
	 * case, it is fairly trivial because we are simply calling ImageJ Services.
	 */
	@Override
	public void run() {
//		try {
//			image = datasetIOService.open(imageFile.getAbsolutePath());
//		}
//		catch (final IOException exc) {
//			// Use the LogService to report the error.
//			logService.error(exc);
//		}
		

		
		
		//setLayout(new FlowLayout(FlowLayout.CENTER,5,5));
		
		
		
		
		
		
	}
	
//////////////////////////MAIN////////////////////////////


	/*
	 * This main method is for convenience - so you can run this command
	 * directly from Eclipse (or other IDE).
	 *
	 * It will launch ImageJ and then run this command using the CommandService.
	 * This is equivalent to clicking "Tutorials>Open Image" in the UI.
	 */
	public static void main(final String... args) throws Exception {
		// Launch ImageJ as usual.
		final ImageJ ij = net.imagej.Main.launch(args);

		// Launch the "OpenImage" command.
		ij.command().run(OpenImage.class, true);
	}
	
	
//////////////METHODS//////////////////////////////////////	
	void addButton(String label, Panel panel) {
		Button b = new Button(label);
		b.addActionListener(this);
		panel.add(b);
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		// TODO Auto-generated method stub
		
		String label = e.getActionCommand();
		if (label==null)
			return;
		
		imp = WindowManager.getCurrentImage();
		numSlices = imp.getStackSize();
		slice = imp.getCurrentSlice();
//		IJ.showMessage("numSlices: "+ numSlices);
//		IJ.showMessage("slice: " + slice);
//		IJ.showMessage("imp: " + imp);
		
		if (label.equals("->"))
			fwd();
		
		if (label.equals("<-"))
			bwd();
	}
	
	private void fwd() {
		// TODO Auto-generated method stub
		imp.setSlice(slice+1);
		imp.updateAndDraw();

	}
	
	private void bwd() {
		// TODO Auto-generated method stub
		imp.setSlice(slice-1);
		imp.updateAndDraw();

	}
	

}
