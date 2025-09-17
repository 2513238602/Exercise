public class Car {
    String manufacturer;
    String model;
    double speed;

    // 构造器：制造商和型号参数，速度初始化为 0
    Car(String manufacturer, String model) {
        if (manufacturer == null || manufacturer.isEmpty()) {
            throw new IllegalArgumentException("Manufacturer cannot be empty.");
        }
        if (model == null || model.isEmpty()) {
            throw new IllegalArgumentException("Model cannot be empty.");
        }
        this.manufacturer = manufacturer;
        this.model = model;
        this.speed = 0.0;
    }

    // 加速方法
    void accelerate(double amount) {
        if (amount < 0) {
            throw new IllegalArgumentException("Acceleration cannot be negative.");
        }
        this.speed += amount;
    }

    // 减速方法，速度不能低于 0
    void decelerate(double amount) {
        if (amount < 0) {
            throw new IllegalArgumentException("Deceleration cannot be negative.");
        }
        this.speed -= amount;
        if (this.speed < 0) {
            this.speed = 0.0;
        }
    }

    // 获取当前速度
    double getCurrentSpeed() {
        return this.speed;
    }

    // 主方法：测试 Car 对象
    public static void main(String[] args) {
        Car car = new Car("Toyota", "Corolla");

        car.accelerate(30.0);
        System.out.println("Current speed: " + car.getCurrentSpeed());

        car.accelerate(20.0);
        System.out.println("Current speed: " + car.getCurrentSpeed());

        car.accelerate(10.0);
        System.out.println("Current speed: " + car.getCurrentSpeed());

        car.decelerate(25.0);
        System.out.println("Current speed: " + car.getCurrentSpeed());

        car.decelerate(50.0); // 尝试过度减速，速度应变为 0
        System.out.println("Current speed: " + car.getCurrentSpeed());
    }
}
