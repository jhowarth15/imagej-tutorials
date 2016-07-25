
import ij.IJ;
import ij.ImagePlus;
import ij.WindowManager;
import ij.gui.GenericDialog;
import ij.gui.ImageWindow;
import ij.plugin.PlugIn;

public class Colour_merge implements PlugIn {
	int nChannels = 2;
	
	public Colour_merge(int chans){
		this.nChannels = chans;
	}
	
	@Override
	public void run(final String arg) {
		final int[] wList = WindowManager.getIDList();
		if (wList == null) {
			IJ.error("No images are open.");
			return;
		}

		final String[] titles = new String[wList.length + 1];
		for (int i = 0; i < wList.length; i++) {
			final ImagePlus imp = WindowManager.getImage(wList[i]);
			titles[i] = imp != null ? imp.getTitle() : "";
		}
		final String none = "*None*";
		titles[wList.length] = none;
		final String[] pscolours =
			{ "<Current>", "Cyan", "Magenta", "Yellow", "Red", "Green", "Blue",
				"Grays" };

		final GenericDialog gd = new GenericDialog("Colour Merge");
		
		gd.addChoice("First Stack:", titles, titles[0]);
		gd.addChoice("First colour", pscolours, pscolours[4]);

		gd.addChoice("Second Stack:", titles, titles[1]);
		gd.addChoice("Second colour", pscolours, pscolours[5]);
		
		if (this.nChannels == 3) {
			gd.addChoice("Third Stack:", titles, titles[2]);
			gd.addChoice("Third colour", pscolours, pscolours[6]);
		}
		
		
		//gd.addCheckbox("Use 'Difference' operator?", false);
		//gd.addCheckbox("Keep source stacks?", true);
		//gd.addNumericField("% of 2 pre-subtracted from 1?", 0, 0);
		//gd.addMessage("When merging brightfield with  fluorescence,\nensure the brightfield image is the first stack");

		gd.showDialog();

		if (gd.wasCanceled()) return;
		final int[] index = new int[3];
		final int[] colourindex = new int[3];

		index[0] = gd.getNextChoiceIndex();
		colourindex[0] = gd.getNextChoiceIndex();

		index[1] = gd.getNextChoiceIndex();
		colourindex[1] = gd.getNextChoiceIndex();
		
		if (this.nChannels == 3) {
			index[2] = gd.getNextChoiceIndex();
			colourindex[2] = gd.getNextChoiceIndex();
		}

//		final boolean UseDiff = false;
//		final boolean keep = false;
//		double preSub = 0;

		final ImagePlus impCh1 = WindowManager.getImage(wList[index[0]]);
		final ImagePlus impCh2 = WindowManager.getImage(wList[index[1]]);
		ImagePlus impCh3 = null;
		
		if (this.nChannels == 3) {
			impCh3 = WindowManager.getImage(wList[index[2]]);
		}

		final String firstcol = pscolours[colourindex[0]];
		final String secondcol = pscolours[colourindex[1]];
		
		String thirdcol = null;
		if (this.nChannels == 3) {
			thirdcol = pscolours[colourindex[2]];
			}

		final ImagePlus[] image = new ImagePlus[3];

		int width = 0;
		for (int i = 0; i < 3; i++) {
			if (index[i] < wList.length) {
				image[i] = WindowManager.getImage(wList[index[i]]);
				width = image[i].getWidth();
			}
		}
		if (width == 0) {
			IJ.error("There must be at least one 8-bit or RGB source stack.");
			return;
		}

//get origina magenta image

		final ImageWindow winCh1 = impCh1.getWindow();
		WindowManager.setCurrentWindow(winCh1);

//duplicate and assign vars
		IJ.run("Duplicate...", "title=Ch1 duplicate");
		IJ.selectWindow("Ch1");
		final ImagePlus impCh1B = WindowManager.getCurrentImage();
		final ImageWindow winCh1B = impCh1B.getWindow();

//get orignial cyan image
		final ImageWindow winCh2 = impCh2.getWindow();
		WindowManager.setCurrentWindow(winCh2);
//Duplicate and assign vars

		IJ.run("Duplicate...", "title=Ch2 duplicate");
		final ImagePlus impCh2B = WindowManager.getCurrentImage();
		final ImageWindow winCh2B = impCh2B.getWindow();
		
		ImagePlus impCh3B = null;
		ImageWindow winCh3 = null;
		ImageWindow winCh3B = null;
		if (this.nChannels == 3) {
			//get orignial cyan image
			winCh3 = impCh3.getWindow();
			WindowManager.setCurrentWindow(winCh3);
			
			//Duplicate and assign vars		
			IJ.run("Duplicate...", "title=Ch3 duplicate");
			impCh3B = WindowManager.getCurrentImage();
			winCh3B = impCh3B.getWindow();
			}
		

//		if (preSub != 0) {
//			WindowManager.setCurrentWindow(winCh2B);
//			IJ.run("Duplicate...", "title=Ch2C duplicate");
//			final ImagePlus impCh2C = WindowManager.getCurrentImage();
//
//			final ImageWindow winCh2C = impCh2C.getWindow();
//			WindowManager.setCurrentWindow(winCh2C);
//			preSub = preSub / 100;
//			IJ.run("Multiply...", "value=" + preSub);
//			IJ.run("Image Calculator...", "image1=Ch1 operation=Subtract image2=Ch2C");
//			impCh2C.changes = false;
//			winCh2C.close();
//		}

		if (this.nChannels == 3) {
			WindowManager.setCurrentWindow(winCh3B);
			if (thirdcol != "<Current>") IJ.run(thirdcol);
			IJ.run("RGB Color");
			
			winCh3.close();
		}
		
		WindowManager.setCurrentWindow(winCh2B);
		if (secondcol != "<Current>") IJ.run(secondcol);
		IJ.run("RGB Color");
		
		winCh2.close();

		WindowManager.setCurrentWindow(winCh1B);
		if (firstcol != "<Current>") IJ.run(firstcol);
		IJ.run("RGB Color");
		
		winCh1.close();


//merge
		IJ.run("Image Calculator...", "image1='Ch1' operation=Add  image2=Ch2 stack");
		
		if (this.nChannels == 3) {
			IJ.run("Image Calculator...", "image1='Ch1' operation=Add  image2=Ch3 stack");
			impCh3B.changes = false;
			winCh3B.close();
		}

//		if (UseDiff == true) IJ.run("Image Calculator...",
//			"image1='Ch1' operation=Difference image2=Ch2 stack");

//rename merge
		IJ.run("Rename...", "title='Colour merge");

		impCh2B.changes = false;

		winCh2B.close();

		IJ.selectWindow("Ch1");
		IJ.run("Rename...", "title='Colour merge'");
	}

}