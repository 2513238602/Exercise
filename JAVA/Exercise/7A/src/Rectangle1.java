public class Rectangle1 {
    double width;
    double height;

    // 无参构造器：保持与步骤 1 的默认 0.0 行为一致
    Rectangle1() {
        this(0.0, 0.0);
    }

    // 带参构造器
    Rectangle1(double width, double height) {
        this.width = width;
        this.height = height;
    }

    // 实例方法：面积
    double calculateArea() {
        return this.width * this.height;
    }

    // 实例方法：周长
    double calculatePerimeter() {
        return 2 * (this.width + this.height);
    }

    public static void main(String[] args) {
        // 按题意：先创建一个 2.5 x 7.5 的矩形
        Rectangle1 r1 = new Rectangle1(2.5, 7.5);
        System.out.println("r1 -> width=" + r1.width + ", height=" + r1.height);
        System.out.println("r1 area=" + r1.calculateArea());
        System.out.println("r1 perimeter=" + r1.calculatePerimeter());

        // 再创建一个无参矩形（应为 0.0, 0.0）
        Rectangle1 r2 = new Rectangle1();
        System.out.println("r2 -> width=" + r2.width + ", height=" + r2.height);
        System.out.println("r2 area=" + r2.calculateArea());
        System.out.println("r2 perimeter=" + r2.calculatePerimeter());
    }
}
