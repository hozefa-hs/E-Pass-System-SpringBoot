package com.porfolio.EPassSystemSpringboot.services.implementations;

import com.porfolio.EPassSystemSpringboot.services.RazorpayService;
import com.razorpay.Order;
import com.razorpay.RazorpayClient;
import com.razorpay.RazorpayException;
import lombok.RequiredArgsConstructor;
import org.json.JSONObject;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
public class RazorpayServiceImpl implements RazorpayService {

    private final RazorpayClient razorpayClient;


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
        return false;
    }
}
