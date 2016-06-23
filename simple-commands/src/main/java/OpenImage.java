/*
 * To the extent possible under law, the ImageJ developers have waived
 * all copyright and related or neighboring rights to this tutorial code.
 *
 * See the CC0 1.0 Universal license for details:
 *     http://creativecommons.org/publicdomain/zero/1.0/
 */

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLConnection;
import java.nio.ByteBuffer;
import java.io.File;

import net.imagej.Dataset;
import net.imagej.ImageJ;

import org.scijava.ItemIO;
import org.scijava.command.Command;
import org.scijava.log.LogService;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;

import com.github.kevinsawicki.http.HttpRequest;

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
import ij.gui.ImageCanvas;
import ij.gui.ImageWindow;
import ij.gui.PlotWindow;
import ij.gui.Roi;
import ij.gui.Toolbar;
import ij.gui.PolygonRoi;
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
import ij.io.DirectoryChooser;

import java.awt.event.MouseListener;
import java.awt.Button;
import java.awt.Desktop;
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
import java.awt.event.MouseEvent;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.util.*;

import javax.imageio.ImageIO;

import java.io.File;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpVersion;
import org.apache.http.ParseException;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.ContentBody;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.CoreProtocolPNames;
import org.apache.http.util.EntityUtils;

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
public class OpenImage extends PlugInFrame implements Command, MouseListener, ActionListener, Measurements {

	OpenImage oi;
	Panel panel;
	Frame instance;
	ImagePlus imp = null;
	ImageStack stack = null;
	ImageCanvas canvas = null;
	ij.gui.PolygonRoi annotations = null;
	int numSlices,slice;
	protected Label label1;
	boolean doScaling = true;
	
	List<int[]> positives = new ArrayList<int[]>();
	List<int[]> negatives = new ArrayList<int[]>();
	
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
		addButton("Train", panel);
		add(panel);
		pack();
 		show();
 		
		DirectoryChooser dc = new DirectoryChooser("Choose a folder");  
		String folder = dc.getDirectory();
		System.out.println("dir: " + folder);
 		

		FolderOpener fo = new FolderOpener();

		
		this.imp = fo.openFolder(folder);
		this.stack = imp.getImageStack();
		
		ImageWindow win = new ImageWindow(imp);
		
		//System.out.println("WIndow got: " + imp.getWindow());
		imp.draw();

		File dir = new File(folder);
		File[] directoryListing = dir.listFiles();
		if (directoryListing != null) {
		    for (File child : directoryListing) {
		      // Do something with child
		    	String file_dir = child.toString();
		    	//System.out.println(file_dir);
		    	//System.out.println("");
		    	if (file_dir.endsWith(".png"))
		    	{
		    		PostFile(file_dir, "127.0.0.1", 9999);
		    	}
		    }
		}    
		
		canvas = imp.getCanvas();
		canvas.addMouseListener(this);
		
		
 		
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
		

//		ImageWindow win = imp.getWindow();
//		canvas = win.getCanvas();
//		canvas.addMouseListener(this);		
		
		
		
		
		
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

//		System.out.println("numSlices: "+ numSlices);
//		System.out.println("slice: " + slice);
//		System.out.println("imp: " + imp);
		
		if (label.equals("->"))
			fwd();
		
		if (label.equals("<-"))
			bwd();
		
		if (label.equals("Train"))
			try {
				train();
			} catch (IOException | URISyntaxException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
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
	
	private void train() throws IOException, URISyntaxException {
		// TODO Auto-generated method stub
		int data = 8;
//		URI uri = new URI("http://localhost:8080/" + data);
//		Desktop dt = Desktop.getDesktop();
//		dt.browse(uri.resolve(uri));

		String response = HttpRequest.get("http://localhost:8080/" + data)
		        .accept("application/json")
		        .body();
		System.out.println("Response was: " + response);
		
	}
	

	@Override
	public void mousePressed(MouseEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void mouseReleased(MouseEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void mouseEntered(MouseEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void mouseExited(MouseEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void mouseClicked(MouseEvent e) {
		// TODO Auto-generated method stub
		
		//IJ.showMessage("e: "+e.getModifiersEx());
		int clicked = e.getModifiersEx();
		
		imp = WindowManager.getCurrentImage();
		ImageProcessor improc = imp.getProcessor();
		
		int x = e.getX();
		int y = e.getY();
		
		int offscreenX = canvas.offScreenX(x);
		int offscreenY = canvas.offScreenY(y);
//		IJ.showMessage("mousePressed: "+offscreenX+","+offscreenY);
		
		//Add positive annotation
		if (clicked == 0){
			improc.setColor(java.awt.Color.white);
			improc.drawRect(x-5,y-5,10,10);
			
			positives.add(new int[] { offscreenX, offscreenY });
			System.out.println("Positives:");
			for (int[] pos : positives) {
		        System.out.print(Arrays.toString(pos) + " ");
		    }
		}
		
		//Add negative annotation - hold shift and left click
		if (clicked == 64){
			improc.setColor(java.awt.Color.red);
			improc.drawRect(x-5,y-5,10,10);
			
			negatives.add(new int[] { offscreenX, offscreenY });
			System.out.println("Negatives:");
			for (int[] neg : negatives) {
		        System.out.print(Arrays.toString(neg) + " ");
		    }
		}
		
		System.out.println("");
		//System.out.println(positives.get(1)[1]);
	}
	
	public static int[][] append(int[][] a, int[][] b) {
        int[][] result = new int[a.length + b.length][];
        System.arraycopy(a, 0, result, 0, a.length);
        System.arraycopy(b, 0, result, a.length, b.length);
        return result;
    }
	
	//Uploads an image file to the python server
	public static void PostFile (String directory, String IP_ADDRESS, int PORT_NO) {
		
        BufferedImage bufferedImage = null;
        try {
            bufferedImage = ImageIO.read(new File(directory));
        } catch (IOException err) {
            err.printStackTrace();
        }
        
        try {
        	Socket socket = new Socket(IP_ADDRESS, PORT_NO);
            OutputStream outputStream = socket.getOutputStream();
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            ImageIO.write(bufferedImage, "png", byteArrayOutputStream);
            // get the size of image
            byte[] size = ByteBuffer.allocate(4).putInt(byteArrayOutputStream.size()).array();
            outputStream.write(size);
            outputStream.write(byteArrayOutputStream.toByteArray());
            outputStream.flush();
        } catch (IOException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        }
		
	}

}
