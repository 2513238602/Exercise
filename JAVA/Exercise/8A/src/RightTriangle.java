public class RightTriangle implements Shape {
    private final double a;
    private final double b;

    public RightTriangle(double a, double b) {
        if (a <= 0 || b <= 0) throw new IllegalArgumentException("legs must be > 0");
        this.a = a; this.b = b;
    }

    @Override public double calculateArea() { return 0.5 * a * b; }
    @Override public double calculatePerimeter() {
        double c = Math.hypot(a, b);
        return a + b + c;
    }
}
