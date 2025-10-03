import java.util.Arrays;

/**
 * 继承 Bucket；新增 name 与可嵌套的 innerBucket。
 * compareTo 基于 capacity。
 */
public class StackableBucket extends Bucket implements Comparable<StackableBucket> {
    private final String name;
    private StackableBucket innerBucket; // 默认 null

    public StackableBucket(double capacity, String name) {
        super(capacity);
        if (name == null) throw new IllegalArgumentException("name cannot be null");
        this.name = name;
        this.innerBucket = null;
    }

    public String getName() { return name; }

    /** 若无内部桶返回 null，否则返回内部桶名字 */
    public String getInnerBucketName() {
        return innerBucket == null ? null : innerBucket.name;
    }

    /**
     * 规则：
     * 1) smallerBucket 容量 >= 本桶 -> 打印并不改状态
     * 2) 若本桶未有 inner，则直接放入
     * 3) 否则递归放入内层
     */
    public void setInnerBucket(StackableBucket smallerBucket) {
        if (smallerBucket == null) return;
        if (smallerBucket.getCapacity() >= this.getCapacity()) {
            System.out.println("Too large to stack!");
            return;
        }
        if (this.innerBucket == null) {
            this.innerBucket = smallerBucket;
        } else {
            this.innerBucket.setInnerBucket(smallerBucket);
        }
    }

    /** 把内部链条全部拆空：this 及链上每个节点 innerBucket=null */
    public void unstackBuckets() {
        StackableBucket cur = this.innerBucket;
        while (cur != null) {
            StackableBucket next = cur.innerBucket;
            cur.innerBucket = null;
            cur = next;
        }
        this.innerBucket = null;
    }

    @Override
    public int compareTo(StackableBucket other) {
        return Double.compare(this.getCapacity(), other.getCapacity());
    }

    public static void main(String[] args) {
        StackableBucket big = new StackableBucket(10.0, "big");
        StackableBucket medium = new StackableBucket(5.0, "medium");
        StackableBucket small = new StackableBucket(1.0, "small");

        // 1) big -> medium（不允许）
        medium.setInnerBucket(big); // Should print: Too large to stack!

        // 2) medium 放进 big
        big.setInnerBucket(medium);
        System.out.println(big.getInnerBucketName()); // "medium"

        // 3) small 放进 big（应进入 medium 里）
        big.setInnerBucket(small);
        System.out.println("big.inner=" + big.getInnerBucketName());       // "medium"
        System.out.println("medium.inner=" + medium.getInnerBucketName()); // "small"

        // 4) 排序演示（Exercise 4）
        StackableBucket[] arr = {small, big, medium};
        Arrays.sort(arr); // 按容量升序
        System.out.println("sorted: " + Arrays.toString(
                Arrays.stream(arr).map(StackableBucket::getName).toArray()));
        // 5) 拆空
        big.unstackBuckets();
        System.out.println("after unstack, big.inner=" + big.getInnerBucketName()); // null
        System.out.println("after unstack, medium.inner=" + medium.getInnerBucketName()); // null
    }
}
