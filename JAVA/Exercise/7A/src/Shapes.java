// 矩形类
class Rectangle {
    double width;
    double height;

    Rectangle(double width, double height) {
        this.width = width;
        this.height = height;
    }

    double calculateArea() {
        return this.width * this.height;
    }

    double calculatePerimeter() {
        return 2 * (this.width + this.height);
    }
}

// 圆形类
class Circle {
    double radius;

    Circle(double radius) {
        this.radius = radius;
    }

    double calculateArea() {
        return Math.PI * this.radius * this.radius;
    }

    double calculatePerimeter() {
        return 2 * Math.PI * this.radius;
    }
}

// 直角三角形类
class RightTriangle {
    double base;
    double height;

    RightTriangle(double base, double height) {
        this.base = base;
        this.height = height;
    }

    double calculateArea() {
        return 0.5 * this.base * this.height;
    }

    double calculatePerimeter() {
        double hypotenuse = Math.sqrt(this.base * this.base + this.height * this.height);
        return this.base + this.height + hypotenuse;
    }
}

// 主类
public class Shapes {
    public static void main(String[] args) {
        Rectangle1 rect = new Rectangle1(3.0, 4.0);
        Circle circle = new Circle(2.0);
        RightTriangle triangle = new RightTriangle(3.0, 4.0);

        System.out.println("Rectangle: area=" + rect.calculateArea()
                + ", perimeter=" + rect.calculatePerimeter());

        System.out.println("Circle: area=" + circle.calculateArea()
                + ", perimeter=" + circle.calculatePerimeter());

        System.out.println("RightTriangle: area=" + triangle.calculateArea()
                + ", perimeter=" + triangle.calculatePerimeter());
    }
}
