/*
 * To the extent possible under law, the ImageJ developers have waived
 * all copyright and related or neighboring rights to this tutorial code.
 *
 * See the CC0 1.0 Universal license for details:
 *     http://creativecommons.org/publicdomain/zero/1.0/
 */

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
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
import org.scijava.util.FileUtils;

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
import ij.gui.Overlay;
import ij.gui.PlotWindow;
import ij.gui.Roi;
import ij.gui.Toolbar;
import ij.gui.PolygonRoi;
import ij.io.Opener;
import ij.io.FileSaver;
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
import java.awt.AlphaComposite;
import java.awt.Button;
import java.awt.Color;
import java.awt.ComponentOrientation;
import java.awt.Desktop;
import java.awt.FileDialog;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Frame;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.Label;
import java.awt.Panel;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.color.ColorSpace;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.awt.image.ColorConvertOp;
import java.util.*;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JSlider;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.filechooser.FileNameExtensionFilter;

import java.io.File;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpVersion;
import org.apache.http.ParseException;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.ContentBody;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.CoreProtocolPNames;
import org.apache.http.util.EntityUtils;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

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


@Plugin(type = Command.class, menuPath = "Josh>Cell Detector")
public class OpenImage extends PlugInFrame implements Command, MouseListener, ActionListener, Measurements {

	OpenImage oi;
	Panel panel;
	Frame instance;
	ImagePlus imp = null;
	ImageStack stack = null;
	ImageCanvas canvas = null;
	ij.gui.PolygonRoi annotations = null;
	int numSlices, slice;
	protected Label label1;
	boolean doScaling = true;
	ImageProcessor improc = null;
	static ImagePlus original = null;
	FolderOpener fo, fop = null;
	String folder, saveFolder = null;
	File selectedFile, selectedFolder = null;
	ImageWindow win = null;
	int nChannels = 2;
	
	List<Overlay> overlay = new ArrayList<Overlay>();
	
	JFileChooser chooser, pngFolder;
	
	List<int[]> positives = new ArrayList<int[]>();
	List<int[]> negatives = new ArrayList<int[]>();
	List<int[]> detections = new ArrayList<int[]>(); //[x,y,prob,nframe]
	int p_value = 50;
	
	JSONObject obj = new JSONObject();
	
	public void run(String arg) {
		IJ.freeMemory();
		oi = new OpenImage();
	}
	
	/**
	 * 
	 */
	public OpenImage() {
		super("Open Image");
		// TODO Auto-generated constructor stub
		//OpenImage instance = this;
		
		setLayout(new FlowLayout());
		panel = new Panel();
		panel.setLayout(new GridLayout());
		

		addButton("<-", panel);
		addButton("->", panel);
		addButton("Train", panel);
		addButton("Test", panel);
		addButton("Test All", panel);
		addButton("Export", panel);
		addButton("Clear", panel);
		
 		//Create the slider
 		JSlider slider = new JSlider(JSlider.HORIZONTAL, 0, 100, 50);
 		slider.addChangeListener(new ChangeListener(){
 		    public void stateChanged(ChangeEvent e) {
 		        JSlider source = (JSlider)e.getSource();
 		        if (!source.getValueIsAdjusting()) {
 		            p_value = (int)source.getValue();
 		            //System.out.print(p_value);
 		           drawAnnotations (p_value);
 		        }    
 		    }
 		});

 		//slider.addChangeListener(this);
 		slider.setMajorTickSpacing(10);
 		slider.setPaintTicks(true);

 		//Create the label table
 		Hashtable<Integer, JLabel> labelTable = new Hashtable<Integer, JLabel>();
 		labelTable.put( new Integer( 0 ), new JLabel("0") );
 		labelTable.put( new Integer( 50 ), new JLabel("Probability (%)") );
 		labelTable.put( new Integer( 100 ), new JLabel("100") );
 		slider.setLabelTable( labelTable );

 		slider.setPaintLabels(true);
 		
		add(panel);
		
		add(slider);
		
		setComponentOrientation(ComponentOrientation.LEFT_TO_RIGHT);
		
		pack();
 		show();
 		
 		
 		//Open png image folder or .tif file 		
		chooser = new JFileChooser();
		chooser.setCurrentDirectory(new java.io.File("../../"));
		chooser.setDialogTitle("Choose a file..");
		chooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
		chooser.setMultiSelectionEnabled(true);
		
		int result = chooser.showOpenDialog(new JFrame());
		if (result == JFileChooser.APPROVE_OPTION) {
			selectedFile = chooser.getSelectedFile();
			folder = selectedFile.toString();
			System.out.println("Selected file: " + selectedFile.getAbsolutePath());
		}
		
		//User selects number of channels
		final String[] gdChannels =
			{ "2", "3" };
		
		GenericDialog gd = new GenericDialog("De-Interleaver");
        gd.addChoice("How many channels?", gdChannels, gdChannels[0]);

        gd.showDialog();

        nChannels = Integer.parseInt(gdChannels[gd.getNextChoiceIndex()]);
        		
		//Logic to deal with selection of tif image or folder of PNGs:
		//If selection is a folder of PNGS
		if (selectedFile.isDirectory()){
			
			fo = new FolderOpener();
			this.imp = fo.openFolder(folder);
			this.stack = imp.getImageStack();
			numSlices = imp.getStackSize();
			
			win = new ImageWindow(imp);
			
			//System.out.println("WIndow got: " + imp.getWindow());
			imp.draw();

			serverUploadAllFiles(folder);
		}
		
		//If selection is tif file
		String tif = ".tif";
		if (selectedFile.isFile() && folder.toLowerCase().contains(tif.toLowerCase())){

			
			this.imp = new ImagePlus(folder);

			win = new ImageWindow(imp);
			
			//System.out.println("WIndow got: " + imp.getWindow());
			imp.draw();
			
			//Break down tif by channel
			DeInterleave_ processTif = new DeInterleave_(nChannels);
			processTif.run("tif");
			
			//Remerge channels with different colours
			Colour_merge mergeChannels = new Colour_merge(nChannels);
			mergeChannels.run("tif");
			
			//Reset imageplus etc to new merged png
			imp = WindowManager.getCurrentImage();
			numSlices = imp.getStackSize();
			slice = imp.getCurrentSlice();
			this.stack = imp.getImageStack();

			//Post images to server as pngs
			for (int s = imp.getCurrentSlice(); s <= numSlices; s++){
				imp.setSlice(s);
				//Send image to server
				BufferedImage buffImage = imp.getBufferedImage();
				PostFile(buffImage, "127.0.0.1", 9999);
			}
			
//			for (int s = 1; s <= numSlices; s++){
				//imp.setSlice(s);
//				BufferedImage buffImage = imp.getBufferedImage();
//	    		PostFile(buffImage, "127.0.0.1", 9999);
//			}
//			
			imp.setSlice(1);
			
		}
		    
		
		canvas = imp.getCanvas();
		canvas.addMouseListener(this);
		
		//Create blank overlay for each frame
		numSlices = imp.getStackSize();
		for (int o = 0; o <= numSlices; o++){
			overlay.add(new Overlay());
		}
		slice = imp.getCurrentSlice();
		imp.setSlice(slice);
		imp.updateAndDraw();
		imp.setOverlay(overlay.get(slice));
		

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
		
		if (label.equals("->")){
			fwd();
			drawAnnotations (p_value);
		}
		
		if (label.equals("<-")){
			bwd();
			drawAnnotations (p_value);
		}
		
		if (label.equals("Train"))
			try {
				train();
			} catch (IOException | URISyntaxException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
		}
		if (label.equals("Test"))
			try {
				test();
			} catch (IOException | URISyntaxException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
		}
		if (label.equals("Test All"))
			testAllFrames();
		
		if (label.equals("Export"))
			try {
				exportToTxt();
			} catch (FileNotFoundException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			} catch (UnsupportedEncodingException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		
		if (label.equals("Clear"))
			clearAnnotsAndDets();
	}
		
	
	private void fwd() {
		if (slice >= numSlices)
			return;
		imp.setSlice(slice+1);
		slice++;
		//System.out.println("New Frame: " + slice);
		imp.updateAndDraw();
		imp.setOverlay(overlay.get(slice));

	}
	
	private void bwd() {
		if (slice == 1)
			return;
		imp.setSlice(slice-1);
		slice--;
		//System.out.println("New Frame: " + slice);
		imp.updateAndDraw();
		imp.setOverlay(overlay.get(slice));

	}
	
	private void train() throws IOException, URISyntaxException {
		
		//Parse the positives and negatives list into Json string and post to URL//
	      int  count = 0;
	      String annotJson = "[{\"pos\": [";
	      for (int[] pos : positives) {
	    	  	String pos_x = String.format("%03d", pos[0]);
	    	  	String pos_y = String.format("%03d", pos[1]);
	    	  	String pos_frame = String.format("%04d", pos[2]);
		        annotJson = annotJson + "\"[" + pos_x + ", " + pos_y + ", " + pos_frame + "]\", ";
		        count++;
		    }
	      //Remove last comma
	      if (count>0)
	      {
	    	  annotJson = annotJson.substring(0, annotJson.length()-2);
	      }
	      count = 0;
	      annotJson = annotJson + "]},{\"neg\": [";
	      for (int[] neg : negatives) {
	    	  	String neg_x = String.format("%03d", neg[0]);
	    	  	String neg_y = String.format("%03d", neg[1]);
	    	  	String neg_frame = String.format("%04d", neg[2]);
		        annotJson = annotJson + "\"[" + neg_x + ", " + neg_y + ", " + neg_frame + "]\", ";
		        count++;
		    }
	      //Remove last comma
	      if (count>0)
	      {
	    	  annotJson = annotJson.substring(0, annotJson.length()-2);
	      }
	      
	      
	      //Add other settings data to json string: nchannels, 
	      annotJson = annotJson + "]},{\"settings\": [" + nChannels + "," + imp.getCurrentSlice() + "," + numSlices;
	      
	      annotJson = annotJson + "]}]";
	      
	      //String jsonText = "[{\"foo\": [\"bar\", \"black\"]},{\"fiz\": \"biz\"}]";  //"data="+out.toString(); //////ISSUES HERE/////////
	      System.out.print(annotJson);
	      
	      httpTrain("http://localhost:8080/", annotJson);
	      		
	}
	
	private void test() throws IOException, URISyntaxException {
		String currentFrame = imp.getCurrentSlice() + "";
		
		//Remove any prev detections on current frame
		for (int count = detections.size()-1; count >= 0; count--){
			if (detections.get(count)[3] == imp.getCurrentSlice()){
				detections.remove(count);
			}
        	
        }
		
		httpTest("http://localhost:8080/test", currentFrame);
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
		improc = imp.getProcessor();
		
		
		int x = e.getX();
		int y = e.getY();
		
		int offscreenX = canvas.offScreenX(x);
		int offscreenY = canvas.offScreenY(y);
//		IJ.showMessage("mousePressed: "+offscreenX+","+offscreenY);
		
		//Add positive annotation
		if (clicked == 0){
//			improc.setColor(java.awt.Color.white);
//			improc.drawRect(x-5,y-5,10,10);
			
			//Display annotation on image
			imp.setOverlay(overlay.get(slice));
			Roi roi = new Roi(x-5,y-5,10,10);
			roi.setStrokeColor(java.awt.Color.blue);
			overlay.get(slice).add(roi);
			
			//Add to arraylist
			positives.add(new int[] { offscreenX, offscreenY, slice });
			System.out.println("Positives:");
			for (int[] pos : positives) {
		        System.out.print(Arrays.toString(pos) + " ");
		    }
		}
		
		//Add negative annotation - hold shift and left click
		if (clicked == 64){
//			improc.setColor(java.awt.Color.red);
//			improc.drawRect(x-5,y-5,10,10);
			
			//Display annotation on image
			imp.setOverlay(overlay.get(slice));
			Roi roi = new Roi(x-5,y-5,10,10);
			roi.setStrokeColor(java.awt.Color.red);
			overlay.get(slice).add(roi);
			
			negatives.add(new int[] { offscreenX, offscreenY, slice });
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
	
	//Uploads an image file to the python server from directory
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
	
	//Uploads an image file to the python server
	public static void PostFile (BufferedImage buffImage, String IP_ADDRESS, int PORT_NO) {
		    
        try {
        	Socket socket = new Socket(IP_ADDRESS, PORT_NO);
            OutputStream outputStream = socket.getOutputStream();
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            ImageIO.write(buffImage, "png", byteArrayOutputStream);
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
	
	public void httpTrain(String url, String body) {

        try (CloseableHttpClient httpClient = HttpClientBuilder.create().build()) {
            
        	HttpPost request = new HttpPost(url);
            StringEntity params = new StringEntity(body);
            request.addHeader("content-type", "application/json");
            request.setEntity(params);
            //HttpResponse result = httpClient.execute(request);
            
            ResponseHandler<String> responseHandler = new BasicResponseHandler();
            httpClient.execute(request, responseHandler);
            
            
        } 
        
        catch (IOException ex) {
        }
        
        clearROIs();
        
        return;
    }
	
	public void httpTest(String url, String body) {

        try (CloseableHttpClient httpClient = HttpClientBuilder.create().build()) {
            
        	HttpPost request = new HttpPost(url);
            StringEntity params = new StringEntity(body);
            request.addHeader("content-type", "application/json");
            request.setEntity(params);
            //HttpResponse result = httpClient.execute(request);
            
            ResponseHandler<String> responseHandler = new BasicResponseHandler();
            String responseBody = httpClient.execute(request, responseHandler);
            //JSONObject response = new JSONObject(responseBody);

            //String json = EntityUtils.toString(result.getEntity(), "UTF-8");
            //System.out.println("RETURNED: " + responseBody);
            
            //Parse response into list of detections
            String[] items = responseBody.replaceAll("\\[", "").replaceAll("\\]", "").replaceAll(" ", "").split(",");
            
            int i = 0;
            while (i < items.length){
                //System.out.println(items[i]);
                detections.add(new int[] { Integer.parseInt(items[i]), Integer.parseInt(items[i+1] ), Integer.parseInt(items[i+2] ), Integer.parseInt(items[i+3] )});
                i = i+4;
            }
            
            System.out.print("Parsed detections::");
            for (int[] det : detections) {
    		        System.out.print(Arrays.toString(det) + " ");
    		    }

            drawAnnotations (p_value);
            
        } catch (IOException ex) {
        }
        return;
    }
	
	void testAllFrames (){
		for (int c = 1; c <= numSlices; c++){
			imp.setSlice(c);
			String cS = c + "";
			httpTest("http://localhost:8080/test", cS);
			drawAnnotations (p_value);
			imp.updateAndDraw();
		}
		imp.setSlice(1);
	}
	
	void drawAnnotations (int probability){
		//Annotate the fiji image with detections


		IJ.freeMemory();
		
		//Replace just the frame/slice in question
		slice = imp.getCurrentSlice();
				
		//Draw the annotations
		if (detections.size() <= 0)
			return;
		
		//overlay.get(slice).clear();
		
		for (int count = 0; count < detections.size()-1; count++){
			if (detections.get(count)[2] > probability && detections.get(count)[3] == imp.getCurrentSlice()){
				int x = detections.get(count)[0];
	        	int y = detections.get(count)[1];
	            
	            imp.setOverlay(overlay.get(slice));
				Roi roi = new Roi(x-5,y-5,10,10);
				roi.setStrokeColor(java.awt.Color.yellow);
				overlay.get(slice).add(roi);
			}
        	
        }
		imp.updateAndDraw();
		
	}
	
	void clearROIs(){
		//Create blank overlay for each frame
		for (int o = 1; o <= numSlices; o++){
			overlay.get(o).clear();
		}
		slice = imp.getCurrentSlice();
		imp.updateAndDraw();
		imp.setOverlay(overlay.get(slice));
	}
	
	void serverUploadAllFiles(String folder){
		File dir = new File(folder);
		File[] directoryListing = dir.listFiles();
		if (directoryListing != null) {
		    for (File child : directoryListing) {
		      // Do something with child
		    	String file_dir = child.toString();
		    	System.out.println(file_dir);
		    	//System.out.println("");
		    	if (file_dir.endsWith(".png"))
		    	{
		    		//Post file to server
		    		PostFile(file_dir, "127.0.0.1", 9999);
		    	}
		    }
		    IJ.showMessage("Images Uploaded");
		}
	}
	
	void exportToTxt() throws FileNotFoundException, UnsupportedEncodingException{
		
		//Choose file save
		JFrame parentFrame = new JFrame();
		 
		JFileChooser fileChooser = new JFileChooser();
		FileNameExtensionFilter filter = new FileNameExtensionFilter("TEXT FILES", "txt", "text");
		fileChooser.setFileFilter(filter);
		fileChooser.setDialogTitle("Specify a .txt file to save");   
		 
		int userSelection = fileChooser.showSaveDialog(parentFrame);
		
		File fileToSave = null;
		if (userSelection == JFileChooser.APPROVE_OPTION) {
		    fileToSave = fileChooser.getSelectedFile();
		    System.out.println("Save as file: " + fileToSave.getAbsolutePath());
		}
		
		//Create detections string for pvalue currently selected
		int no_dets = 0;
        for (@SuppressWarnings("unused") int[] det : detections) {
		        no_dets++;
		    }
        
        PrintWriter writer = new PrintWriter(fileToSave, "UTF-8");
        String export = "";
		for (int count = 0; count < no_dets; count++){
			if (detections.get(count)[2] > p_value){
				int x = detections.get(count)[0];
	        	int y = detections.get(count)[1];
	        	int f = detections.get(count)[3];
	            
	        	export = x + "," + y + "," + f;
	        	writer.println(export);
			}
        	
        }
		
		writer.close();
	}
	
	void clearAnnotsAndDets() {
		positives.clear();
		negatives.clear();
		detections.clear();
		drawAnnotations (p_value);
		imp.updateAndDraw();
	}
	
}
