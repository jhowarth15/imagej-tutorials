import imagej.Priority;
import imagej.data.Data;
import imagej.data.display.DataView;
import imagej.data.display.DefaultOverlayView;
import imagej.plugin.Plugin;

@Plugin(type = DataView.class, priority = Priority.HIGH_PRIORITY)
public class CircleOverlayView extends DefaultOverlayView {

	@Override
	public boolean isCompatible(final Data data) {
		return data != null && CircleOverlay.class.isAssignableFrom(data.getClass());
	}

	@Override
	public void update() {
		CircleOverlay data = (CircleOverlay)getData();
		
	}

	@Override
	public void rebuild() {
		
	}

}
