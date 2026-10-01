package com.porfolio.EPassSystemSpringboot.services;

import com.razorpay.RazorpayException;

import java.math.BigDecimal;

public interface RazorpayService {

    String createOrder(BigDecimal amount, String receipt) throws RazorpayException;

    boolean verifyPaymentSignature(String orderId, String paymentId, String signature)throws RazorpayException;

    String getKeyId();

}
