package org.ndx.codingame.lib2d;

import java.util.Collection;

import org.ndx.codingame.lib2d.base.AbstractPoint;
import org.ndx.codingame.lib2d.continuous.ContinuousPoint;

public class Line implements PointBuilder<ContinuousPoint> {
	public static class Coeffs {
		public final double a;
		public final double b;
		public final double c;
		public final double lineNorm;
		public final double squareNorm;
		public Coeffs(ContinuousPoint first, ContinuousPoint second) {
			this.a = second.y-first.y;
			this.b = first.x-second.x; 
			this.c = (first.y-second.y)*first.x + (second.x-first.x)*first.y;
			this.squareNorm = first.distance2SquaredTo(second);
			this.lineNorm = first.distance2To(second);
		}
		public double lineEquationFor(AbstractPoint point) {
			return a*point.getX()+b*point.getY()+c;
		}

		protected double computeXFromY(double y) {
			if(isHorizontalLine())
				throw new UnsupportedOperationException("can't compute x from y on vertical or horizontal line");
			return -(b*y+c)/a;
		}

		protected double computeYFromX(double x) {
			if(isVerticalLine())
				throw new UnsupportedOperationException("can't compute y from x on vertical or horizontal line");
			return -(a*x+c)/b;
		}
		public boolean isVerticalLine() {
			return Algebra.isZero(b);
		}
		public boolean isHorizontalLine() {
			return Algebra.isZero(a);
		}
		public boolean matches(ContinuousPoint point) {
			return Algebra.isZero(a*point.x+b*point.y+c);
		}
	}
	public class SegmentBuilder {

		private ContinuousPoint start;

		public SegmentBuilder startingAt(ContinuousPoint second) {
			start = second;
			return this;
		}

		/**
		 * Here, length is an absolute one, as opposed to the one defined in {@link Line#pointAtNTimes(double)}
		 * @param length
		 * @return a Segment using the two points
		 */
		public Segment ofLength(double length) {
			// first, get distance between the two points of the line
			double referenceDistance = first.distance2To(second);
			double multiplier = length/referenceDistance;
			return new Segment(start, pointAtNTimesOf(start, multiplier, Line.this));
		}
		
	}
	public final ContinuousPoint first;
	public final ContinuousPoint second;
	public final Coeffs coeffs;
	public Line(ContinuousPoint first, ContinuousPoint second) {
		super();
		this.first = first;
		this.second = second;
		this.coeffs = new Coeffs(first, second);
	}

	public double distance2To(AbstractPoint point) {
		return Math.abs(coeffs.lineEquationFor(point))/coeffs.lineNorm;
	}
	
	public boolean contains(ContinuousPoint point) {
		return coeffs.matches(point);
	}

	public ContinuousPoint project(ContinuousPoint point) {
		return project(point, PointBuilder.DEFAULT);
	}

	public <Type extends ContinuousPoint> Type project(Type point, PointBuilder<Type> builder) {
		if(distance2To(point)<0.001)
			return point;
		double projection = ((second.x-first.x)*(point.x-second.x)+(second.y-first.y)*(point.y-second.y))/coeffs.squareNorm;
		// coordinates
		double mx = second.x+(second.x-first.x)*projection;
		double my = second.y+(second.y-first.y)*projection;
		return builder.build(mx, my);
	}

	public ContinuousPoint symetricOf(ContinuousPoint point) {
		return symetricOf(point, PointBuilder.DEFAULT);
	}
		
	public <Type extends ContinuousPoint> Type symetricOf(Type point, PointBuilder<Type> builder) {
		if(distance2To(point)<0.001)
			return point;
		ContinuousPoint projected = project(point);
		Line orthogonal = new Line(point, projected);
		return orthogonal.pointAtNTimes(2, builder);
	}

	/**
	 * Get the point at n times the distance between first and second point
	 * @param i
	 * @return
	 */
	public ContinuousPoint pointAtNTimes(double i) {
		return pointAtNTimesOf(first, i, this);
	}

	public <Type extends ContinuousPoint> Type  pointAtNTimes(double i, PointBuilder<Type> builder) {
		return pointAtNTimesOf(first, i, builder);
	}

	protected <Type extends ContinuousPoint> Type pointAtNTimesOf(ContinuousPoint p, double i, PointBuilder<Type> builder) {
		double x = p.x;
		double y = p.y;
		if(Algebra.isZero(first.distance2To(second)))
			return builder.build(x, y);
		if(coeffs.isHorizontalLine()) {
			y = p.y;
			x = i*(second.x-first.x)+p.x;
		} else if(coeffs.isVerticalLine()) {
			x = p.x;
			y = i*(second.y-first.y)+p.y;
		} else {
			x = i*(second.x-first.x)+p.x;
			y = coeffs.computeYFromX(x);
		}
		return builder.build(x, y);
	}

	public double angleWith(Line aim) {
		return aim.angle()-angle();
	}

	public double angle() {
		return Math.toDegrees(Math.atan2(second.y-first.y, second.x-first.x));
	}
	

	public SegmentBuilder segment() {
		return new SegmentBuilder();
	}

	public Segment getDefiningSegment() {
		return new Segment(first, second);
	}	
	@Override
	public String toString() {
		return "Line [first=" + first + ", second=" + second + "]";
	}
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((first == null) ? 0 : first.hashCode());
		result = prime * result + ((second == null) ? 0 : second.hashCode());
		return result;
	}
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Line other = (Line) obj;
		if (first == null) {
			if (other.first != null)
				return false;
		} else if (!first.equals(other.first))
			return false;
		if (second == null) {
			if (other.second != null)
				return false;
		} else if (!second.equals(other.second))
			return false;
		return true;
	}

	@Override
	public ContinuousPoint build(double x, double y) {
		if(x==first.x && y==first.y)
			return first;
		else if(x==second.x && y==second.y)
			return second;
		return new ContinuousPoint(x, y);
	}

	public <Type extends ContinuousPoint> Type pointAtAngle(ContinuousPoint center, int angle, double radius, PointBuilder<Type> builder) {
		double combinedAngle = Math.toRadians(angle()+angle);
		return builder.build(
				center.x+Math.cos(combinedAngle)*radius,
				center.y+Math.sin(combinedAngle)*radius
				);
	}
	
	public Collection<ContinuousPoint> intersectionWith(Circle circle) {
		return circle.intersectionWith(this);
	}
}
