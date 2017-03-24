package CurveFeverClone;

class Point {
	double x;
	double y;
	
	Point() {
		x = 0;
		y = 0;
	}
	
	Point(double _x, double _y) {
		x = _x;
		y = _y;
	}

	Point copy(Point p) {
	  this.x = p.x;
	  this.y = p.y;

    return this;
  }
}