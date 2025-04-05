package ra.doantotnghiep2025.controller;

import com.paypal.api.payments.Links;
import com.paypal.api.payments.Payment;
import com.paypal.base.rest.PayPalRESTException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import ra.doantotnghiep2025.service.PayPalService;

@RestController
@RequestMapping("/api/paypal")
public class PayPalController {

    @Autowired
    private PayPalService payPalService;

    @GetMapping("/pay")
    public String payment(
            @RequestParam Long userId,
            @RequestParam String receiveAddress,
            @RequestParam String receiveName,
            @RequestParam String receivePhone,
            @RequestParam(required = false) String note) {
        try {
            Payment payment = payPalService.createPayment(
                    10.0, // Tổng tiền (có thể thay đổi tùy theo logic của bạn)
                    "USD", // Loại tiền tệ
                    "paypal", // Phương thức thanh toán
                    "sale", // Ý định (sale: thanh toán ngay)
                    "Thanh toán đơn hàng", // Mô tả
                    userId,
                    receiveAddress,
                    receiveName,
                    receivePhone,
                    note
            );

            for (Links links : payment.getLinks()) {
                if (links.getRel().equals("approval_url")) {
                    return "redirect:" + links.getHref(); // Chuyển hướng người dùng đến PayPal
                }
            }
        } catch (PayPalRESTException e) {
            e.printStackTrace();
        }
        return "redirect:/error";
    }

    @GetMapping("/success")
    public String successPay(
            @RequestParam("paymentId") String paymentId,
            @RequestParam("PayerID") String payerId,
            @RequestParam("userId") Long userId,
            @RequestParam("receiveAddress") String receiveAddress,
            @RequestParam("receiveName") String receiveName,
            @RequestParam("receivePhone") String receivePhone,
            @RequestParam(value = "note", required = false) String note) {
        try {
            Payment payment = payPalService.executePayment(paymentId, payerId);
            if (payment.getState().equals("approved")) {
                return "Thanh toán thành công!";
            }
        } catch (PayPalRESTException e) {
            e.printStackTrace();
        }
        return "Có lỗi xảy ra!";
    }

    @GetMapping("/cancel")
    public String cancelPay() {
        return "Thanh toán đã bị hủy!";
    }
}