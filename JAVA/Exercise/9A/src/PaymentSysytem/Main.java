package PaymentSysytem;
public class Main {
    public static void main(String[] args) {
        // 创建PayPalProcessor和CreditCardProcessor实例
        // 编译时类型为PaymentProcessor
        PaymentProcessor paypalProcessor = new PayPalProcessor("user@example.com");
        PaymentProcessor creditCardProcessor = new CreditCardProcessor("1234-5678-9012-3456", "12/25");

        // 调用processPayment(double amount)方法
        System.out.println("调用processPayment(double)方法:");
        paypalProcessor.processPayment(300.0);
        creditCardProcessor.processPayment(250.0);

        // 尝试调用CreditCardProcessor的重载方法
        System.out.println("\n尝试调用带transactionID的processPayment方法:");
        // 直接调用会编译错误，因为编译时类型是PaymentProcessor
        // creditCardProcessor.processPayment(25.0, "TXN12345");

        // 解决方法：类型转换
        if (creditCardProcessor instanceof CreditCardProcessor) {
            ((CreditCardProcessor) creditCardProcessor).processPayment(25.0, "TXN12345");
        }

        // 尝试调用PayPalProcessor的重载方法
        System.out.println("\n尝试在PayPalProcessor上调用带transactionID的方法:");
        // 这会编译错误，因为PayPalProcessor没有这个方法
        if (paypalProcessor instanceof PayPalProcessor) {
            // 即使进行类型转换也无法调用，因为PayPalProcessor没有定义这个方法
            // ((PayPalProcessor) paypalProcessor).processPayment(50.0, "TXN67890");
            System.out.println("无法调用，因为PayPalProcessor没有带transactionID的processPayment方法");
        }
    }
}

