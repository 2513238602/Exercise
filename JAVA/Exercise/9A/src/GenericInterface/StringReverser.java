package GenericInterface;

public class StringReverser implements DataProcessor<String> {
    @Override
    public String process(String data) {
        // 反转输入的字符串
        if (data == null) {
            return null;
        }
        StringBuilder reversed = new StringBuilder(data);
        return reversed.reverse().toString();
    }
}
