package ra.doantotnghiep2025.service;

import com.paypal.api.payments.*;
import com.paypal.base.rest.APIContext;
import com.paypal.base.rest.PayPalRESTException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

@Service
public class PayPalService {

    @Autowired
    private APIContext apiContext;

    @Value("${paypal.return-url}")
    private String returnUrl;

    @Value("${paypal.cancel-url}")
    private String cancelUrl;

    public Payment createPayment(
            Double total,
            String currency,
            String method,
            String intent,
            String description,
            Long userId,
            String receiveAddress,
            String receiveName,
            String receivePhone,
            String note) throws PayPalRESTException {

        // Chuyển đổi total thành BigDecimal để làm tròn
        BigDecimal totalBigDecimal = BigDecimal.valueOf(total).setScale(2, RoundingMode.HALF_UP);
        Amount amount = new Amount();
        amount.setCurrency(currency);
        amount.setTotal(totalBigDecimal.toString());

        Transaction transaction = new Transaction();
        transaction.setDescription(description);
        transaction.setAmount(amount);

        List<Transaction> transactions = new ArrayList<>();
        transactions.add(transaction);

        Payer payer = new Payer();
        payer.setPaymentMethod(method);

        Payment payment = new Payment();
        payment.setIntent(intent);
        payment.setPayer(payer);
        payment.setTransactions(transactions);

        // Tạo success URL với các query parameters
        String successUrl = returnUrl + "?userId=" + userId +
                "&receiveAddress=" + encodeURIComponent(receiveAddress) +
                "&receiveName=" + encodeURIComponent(receiveName) +
                "&receivePhone=" + encodeURIComponent(receivePhone) +
                "&note=" + encodeURIComponent(note != null ? note : "");

        RedirectUrls redirectUrls = new RedirectUrls();
        redirectUrls.setCancelUrl(cancelUrl);
        redirectUrls.setReturnUrl(successUrl);
        payment.setRedirectUrls(redirectUrls);

        return payment.create(apiContext);
    }

    public Payment executePayment(String paymentId, String payerId) throws PayPalRESTException {
        Payment payment = new Payment();
        payment.setId(paymentId);
        PaymentExecution paymentExecution = new PaymentExecution();
        paymentExecution.setPayerId(payerId);
        return payment.execute(apiContext, paymentExecution);
    }

    // Helper method to encode URL parameters
    private String encodeURIComponent(String value) {
        try {
            return java.net.URLEncoder.encode(value, "UTF-8");
        } catch (Exception e) {
            return value;
        }
    }
}