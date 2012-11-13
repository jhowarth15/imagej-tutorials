/*
 * To the extent possible under law, the ImageJ developers have waived
 * all copyright and related or neighboring rights to this tutorial code.
 *
 * See the CC0 1.0 Universal license for details:
 *     http://creativecommons.org/publicdomain/zero/1.0/
 */

import imagej.ImageJ;
import imagej.data.display.ImageDisplay;
import imagej.data.display.ImageDisplayService;
import imagej.data.display.OverlayService;
import imagej.data.overlay.Overlay;
import imagej.display.event.input.MsMovedEvent;
import imagej.plugin.Plugin;
import imagej.tool.AbstractTool;
import imagej.tool.Tool;
import imagej.tool.ToolService;

import java.util.Collections;

import net.imglib2.meta.Axes;

/** A command that generates a diagonal gradient image of user-given size. */
@Plugin(type = Tool.class)
public class CircleTool extends AbstractTool {

	private ImageDisplayService imageDisplayService;
	
	private OverlayService overlayService;

	@Override
	public void setContext(final ImageJ context) {
		super.setContext(context);
		imageDisplayService = context.getService(ImageDisplayService.class);
		overlayService = context.getService(OverlayService.class);
	}

	@Override
	public void onMouseMove(final MsMovedEvent evt) {
		final ImageDisplay display = evt.getContext().getService(ImageDisplayService.class).getActiveImageDisplay();
		final double x = display.calibration(display.getAxisIndex(Axes.X));
		final double y = display.calibration(display.getAxisIndex(Axes.Y));

		final Overlay circle = new CircleOverlay(x, y, 30);
		overlayService.addOverlays(imageDisplayService.getActiveImageDisplay(), Collections.singletonList(circle));
	}

	/** Tests our command. */
	public static void main(final String... args) throws Exception {
		// Launch ImageJ as usual.
		final ImageJ context = imagej.Main.launch(args);

		// Launch the "Gradient Image" command right away.
		final ToolService toolService =
			context.getService(ToolService.class);
		toolService.setActiveTool(new CircleTool());
	}

}
