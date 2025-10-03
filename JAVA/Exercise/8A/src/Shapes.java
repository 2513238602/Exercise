import java.util.Arrays;

public class Shapes {
    /** 返回 [area, perimeter] */
    public static double[] computeAreaAndPerimeter(Shape s) {
        return new double[]{ s.calculateArea(), s.calculatePerimeter() };
    }

    public static void main(String[] args) {
        Shape r = new Rectangle(3, 4);
        Shape c = new Circle(2);
        Shape t = new RightTriangle(3, 4);

        for (Shape s : new Shape[]{r, c, t}) {
            double[] ap = computeAreaAndPerimeter(s);
            System.out.println(s.getClass().getSimpleName() + " -> " + Arrays.toString(ap));
        }
    }
}
