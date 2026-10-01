package com.porfolio.EPassSystemSpringboot.services.implementations;

import com.porfolio.EPassSystemSpringboot.services.RazorpayService;
import com.razorpay.Order;
import com.razorpay.RazorpayClient;
import com.razorpay.RazorpayException;
import com.razorpay.Utils;
import lombok.RequiredArgsConstructor;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
public class RazorpayServiceImpl implements RazorpayService {

    private final RazorpayClient razorpayClient;

    @Value("${razorpay.key.secret}")
    private String keySecret;

    @Value("${razorpay.key.id}")
    private String keyId;

    @Override
    public String createOrder(BigDecimal amount, String receipt) throws RazorpayException {

        long amountInPaise = amount.multiply(BigDecimal.valueOf(100)).longValueExact();

        JSONObject orderRequest = new JSONObject();

        orderRequest.put("amount", amountInPaise);
        orderRequest.put("currency", "INR");
        orderRequest.put("receipt", receipt);

        Order order = razorpayClient.orders.create(orderRequest);

        return order.get("id");

    }


    @Override
    public boolean verifyPaymentSignature(String orderId, String paymentId, String signature) throws RazorpayException {

        JSONObject options = new JSONObject();

        options.put("razorpay_order_id", orderId);
        options.put("razorpay_payment_id", paymentId);
        options.put("razorpay_signature", signature);

        return Utils.verifyPaymentSignature(options, keySecret);

    }

    @Override
    public String getKeyId() {
        return keyId;
    }
}
