package GenericInterface;

public class IntegerMultiplier implements DataProcessor<Integer> {
    @Override
    public Integer process(Integer data) {
        // 将输入的整数翻倍
        return data * 2;
    }
}
