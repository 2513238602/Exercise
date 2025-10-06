package GeometricShapes;

// 主类
public class Main {
    public static void main(String[] args) {
        // 创建3个Circle实例
        Circle circle1 = new Circle(2.0);
        Circle circle2 = new Circle(3.0);
        Circle circle3 = new Circle(4.0);

        // 创建3个Rectangle实例
        Rectangle rect1 = new Rectangle(2.0, 3.0);
        Rectangle rect2 = new Rectangle(4.0, 5.0);
        Rectangle rect3 = new Rectangle(6.0, 7.0);

        // 创建Circle容器并添加圆形
        ShapeContainer<Circle> circleContainer = new ShapeContainer<>();
        circleContainer.addShape(circle1);
        circleContainer.addShape(circle2);
        circleContainer.addShape(circle3);

        // 创建Rectangle容器并添加矩形
        ShapeContainer<Rectangle> rectContainer = new ShapeContainer<>();
        rectContainer.addShape(rect1);
        rectContainer.addShape(rect2);
        rectContainer.addShape(rect3);

        // 打印每个容器的总面积
        System.out.printf("Total area of circles: %.2f%n", circleContainer.getTotalArea());
        System.out.printf("Total area of rectangles: %.2f%n", rectContainer.getTotalArea());

        // 测试ShapeContainer<Shape>
        ShapeContainer<Shape> shapeContainer = new ShapeContainer<>();
        shapeContainer.addShape(circle1);
        shapeContainer.addShape(rect1);
        shapeContainer.addShape(circle2);
        shapeContainer.addShape(rect2);
        System.out.printf("Total area of mixed shapes: %.2f%n", shapeContainer.getTotalArea());
    }
}
