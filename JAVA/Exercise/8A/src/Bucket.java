/**
 * 不变式：capacity >= 0；0 <= contents <= capacity
 */
public class Bucket {
    private final double capacity;
    private double contents;

    public Bucket(double capacity) {
        if (capacity < 0) throw new IllegalArgumentException("capacity must be >= 0");
        this.capacity = capacity;
        this.contents = 0.0;
    }

    public double getCapacity() { return capacity; }
    public double getContents() { return contents; }

    /** 倒空并返回倒空前的含量 */
    public double empty() {
        double before = contents;
        contents = 0.0;
        return before;
    }

    /** 添加但不超过容量；负数视为不操作 */
    public void add(double amount) {
        if (amount <= 0) return;
        contents = Math.min(capacity, contents + amount);
    }

    public static void main(String[] args) {
        Bucket big = new Bucket(10.0);
        Bucket small = new Bucket(1.0);

        big.add(20.0);
        small.add(20.0);
        System.out.println("big=" + big.getContents());     // 10.0
        System.out.println("small=" + small.getContents()); // 1.0

        System.out.println("emptied=" + big.empty());       // 10.0
        System.out.println("big now=" + big.getContents()); // 0.0

        // “把 small 倒入 big”的简单演示：取出 small 的水再加到 big
        double moved = small.empty();
        big.add(moved);

        System.out.println("small=" + small.getContents()); // 0.0
        System.out.println("big=" + big.getContents());     // 1.0
    }
}
