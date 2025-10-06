package GenericInterface;

public class Main {
    public static void main(String[] args) {
        // 处理整数
        Integer originalInt = 15;
        DataProcessor<Integer> intProcessor = new IntegerMultiplier();
        Integer processedInt = DataHandler.handleData(originalInt, intProcessor);

        System.out.println("Processing integer:");
        System.out.println("Original: " + originalInt);
        System.out.println("Processed: " + processedInt);

        // 处理字符串
        String originalStr = "Hello, World!";
        DataProcessor<String> strProcessor = new StringReverser();
        String processedStr = DataHandler.handleData(originalStr, strProcessor);

        System.out.println("\nProcessing string:");
        System.out.println("Original: " + originalStr);
        System.out.println("Processed: " + processedStr);
    }
}
