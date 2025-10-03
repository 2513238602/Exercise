public class Rectangle implements Shape {
    private final double width;
    private final double height;

    public Rectangle(double width, double height) {
        if (width <= 0 || height <= 0) throw new IllegalArgumentException("width/height must be > 0");
        this.width = width;
        this.height = height;
    }

    @Override public double calculateArea() { return width * height; }
    @Override public double calculatePerimeter() { return 2 * (width + height); }
}
