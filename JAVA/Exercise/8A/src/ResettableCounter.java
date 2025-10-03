/** 可重置的计数器：在 Counter 语义上增加 reset 能力，不破坏父类契约 */
public class ResettableCounter extends Counter {
    public void reset() { count = 0; }
}
