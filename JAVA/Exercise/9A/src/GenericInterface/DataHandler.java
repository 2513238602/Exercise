package GenericInterface;

public class DataHandler {
    // 泛型静态方法，处理数据
    public static <T> T handleData(T data, DataProcessor<T> processor) {
        return processor.process(data);
    }
}
