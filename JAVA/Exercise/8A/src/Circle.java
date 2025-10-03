public class Circle implements Shape {
    private final double radius;

    public Circle(double radius) {
        if (radius <= 0) throw new IllegalArgumentException("radius must be > 0");
        this.radius = radius;
    }

    @Override public double calculateArea() { return Math.PI * radius * radius; }
    @Override public double calculatePerimeter() { return 2 * Math.PI * radius; }
}
