package PaymentSysytem;

public class CreditCardProcessor extends PaymentProcessor {
    private String cardNumber;
    private String expiryDate;

    // 构造函数
    public CreditCardProcessor(String cardNumber, String expiryDate) {
        this.cardNumber = cardNumber;
        this.expiryDate = expiryDate;
    }

    // 重写支付处理方法
    @Override
    public void processPayment(double amount) {
        System.out.printf("Processing %.2f AUD via Credit Card. Card number: %s%n", amount, cardNumber);
    }

    // 重载支付处理方法，添加交易ID参数
    public void processPayment(double amount, String transactionID) {
        System.out.printf("Processing %.2f AUD via Credit Card. Card number: %s. Transaction ID: %s%n",
                amount, cardNumber, transactionID);
    }
}
