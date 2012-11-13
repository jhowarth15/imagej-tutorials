import imagej.data.overlay.AbstractOverlay;

public class CircleOverlay extends AbstractOverlay {

	private double x, y, r;

	public CircleOverlay(double x, double y, double r) {
		this.x = x;
		this.y = y;
		this.r = r;
	}

	@Override
	public void move(double[] deltas) {
		x += deltas[0];
		y += deltas[1];
	}

	public double getX() {
		return x;
	}

	public double getY() {
		return y;
	}

	public double getRadius() {
		return r;
	}

}
