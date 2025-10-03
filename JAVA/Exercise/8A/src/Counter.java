/** 单调递增计数器：increase 后 getCount() 不减少 */
public class Counter implements Countable {
    protected int count = 0;

    @Override public int getCount() { return count; }

    public void increase() {
        if (count == Integer.MAX_VALUE) {
            throw new IllegalStateException("counter overflow");
        }
        count++;
    }
}
