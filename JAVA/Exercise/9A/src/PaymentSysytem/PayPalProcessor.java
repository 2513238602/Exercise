package PaymentSysytem;

public class PayPalProcessor extends PaymentProcessor {
    private String email;

    // 构造函数
    public PayPalProcessor(String email) {
        this.email = email;
    }

    // 重写支付处理方法
    @Override
    public void processPayment(double amount) {
        System.out.printf("Processing %.2f AUD via PayPal. User email: %s%n", amount, email);
    }
}
