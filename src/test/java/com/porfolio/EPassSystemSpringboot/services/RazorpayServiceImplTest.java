package com.porfolio.EPassSystemSpringboot.services;


import com.porfolio.EPassSystemSpringboot.services.implementations.RazorpayServiceImpl;
import com.razorpay.*;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.HexFormat;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RazorpayServiceImplTest {

    private static final String KEY_ID = "rzp_test_123456789";

    private static final String KEY_SECRET = "test-secret-key";

    private static final String ORDER_ID = "order_test_123";

    private static final String PAYMENT_ID = "pay_test_456";

    @Mock
    private OrderClient orderClient;

    @Mock
    private PaymentClient paymentClient;

    @Mock
    private Order order;

    @Mock
    private Payment payment;

    private RazorpayClient razorpayClient;

    private RazorpayServiceImpl razorpayService;


    @BeforeEach
    void setUp() throws RazorpayException {

        // Arrange
        razorpayClient = new RazorpayClient("test-key", "test-secret");

        ReflectionTestUtils.setField(razorpayClient, "orders", orderClient);

        ReflectionTestUtils.setField(razorpayClient, "payments", paymentClient);

        razorpayService = new RazorpayServiceImpl(razorpayClient);

        ReflectionTestUtils.setField(razorpayService, "keyId", KEY_ID);

        ReflectionTestUtils.setField(razorpayService, "keySecret", KEY_SECRET);
    }


    // -------------------------------------------------------------------------
    // createOrder
    // -------------------------------------------------------------------------

    @Test
    void createOrder_validAmount_createsInrOrderAndReturnsOrderId() throws RazorpayException, JSONException {

        // Arrange
        BigDecimal amount = new BigDecimal("499.00");

        String receipt = "PAYMENT-101";

        when(orderClient.create(any(JSONObject.class))).thenReturn(order);

        when(order.get("id")).thenReturn(ORDER_ID);

        // Act
        String result = razorpayService.createOrder(amount, receipt);

        // Assert
        assertEquals(ORDER_ID, result);

        ArgumentCaptor<JSONObject> requestCaptor = ArgumentCaptor.forClass(JSONObject.class);

        verify(orderClient).create(requestCaptor.capture());

        JSONObject request = requestCaptor.getValue();

        assertEquals(49900L, request.getLong("amount"));

        assertEquals("INR", request.getString("currency"));

        assertEquals(receipt, request.getString("receipt"));

        verify(order).get("id");
    }

    @Test
    void createOrder_decimalAmount_convertsRupeesToExactPaise() throws RazorpayException, JSONException {

        // Arrange
        BigDecimal amount = new BigDecimal("299.35");

        String receipt = "PAYMENT-102";

        when(orderClient.create(any(JSONObject.class))).thenReturn(order);

        when(order.get("id")).thenReturn(ORDER_ID);

        // Act
        String result = razorpayService.createOrder(amount, receipt);

        // Assert
        assertEquals(ORDER_ID, result);

        ArgumentCaptor<JSONObject> requestCaptor = ArgumentCaptor.forClass(JSONObject.class);

        verify(orderClient).create(requestCaptor.capture());

        JSONObject request = requestCaptor.getValue();

        assertEquals(29935L, request.getLong("amount"));

        assertEquals("INR", request.getString("currency"));

        assertEquals(receipt, request.getString("receipt"));
    }

    @Test
    void createOrder_amountWithFractionalPaise_throwsArithmeticExceptionWithoutCallingRazorpay() throws RazorpayException {

        // Arrange
        BigDecimal amount = new BigDecimal("10.005");

        // Act
        assertThrows(ArithmeticException.class, () -> razorpayService.createOrder(amount, "PAYMENT-103"));

        // Assert
        verify(orderClient, never()).create(any(JSONObject.class));
    }

    @Test
    void createOrder_nullAmount_currentBehaviorThrowsNullPointerExceptionWithoutCallingRazorpay() throws RazorpayException {

        // Arrange

        // Act
        assertThrows(NullPointerException.class, () -> razorpayService.createOrder(null, "PAYMENT-104"));

        // Assert
        verify(orderClient, never()).create(any(JSONObject.class));
    }

    @Test
    void createOrder_razorpayFails_propagatesSameRazorpayException() throws RazorpayException {

        // Arrange
        BigDecimal amount = new BigDecimal("500.00");

        RazorpayException razorpayException = org.mockito.Mockito.mock(RazorpayException.class);

        when(orderClient.create(any(JSONObject.class))).thenThrow(razorpayException);

        // Act
        RazorpayException exception = assertThrows(RazorpayException.class, () -> razorpayService.createOrder(amount, "PAYMENT-105"));

        // Assert
        assertSame(razorpayException, exception);

        verify(orderClient).create(any(JSONObject.class));
    }


    // -------------------------------------------------------------------------
    // verifyPaymentSignature
    // -------------------------------------------------------------------------

    @Test
    void verifyPaymentSignature_validSignature_returnsTrue() throws Exception {

        // Arrange
        String signature = generateSignature(ORDER_ID, PAYMENT_ID, KEY_SECRET);

        // Act
        boolean result = razorpayService.verifyPaymentSignature(ORDER_ID, PAYMENT_ID, signature);

        // Assert
        assertTrue(result);
    }

    @Test
    void verifyPaymentSignature_invalidSignature_returnsFalse() throws RazorpayException {

        // Arrange
        String invalidSignature = "0000000000000000000000000000000000000000000000000000000000000000";

        // Act
        boolean result = razorpayService.verifyPaymentSignature(ORDER_ID, PAYMENT_ID, invalidSignature);

        // Assert
        assertFalse(result);
    }


    // -------------------------------------------------------------------------
    // getKeyId
    // -------------------------------------------------------------------------

    @Test
    void getKeyId_configuredKeyId_returnsKeyId() {

        // Arrange

        // Act
        String result = razorpayService.getKeyId();

        // Assert
        assertEquals(KEY_ID, result);
    }


    // -------------------------------------------------------------------------
    // fetchPayment
    // -------------------------------------------------------------------------

    @Test
    void fetchPayment_existingPayment_returnsFetchedPayment() throws RazorpayException {

        // Arrange
        when(paymentClient.fetch(PAYMENT_ID)).thenReturn(payment);

        // Act
        Payment result = razorpayService.fetchPayment(PAYMENT_ID);

        // Assert
        assertSame(payment, result);

        verify(paymentClient).fetch(PAYMENT_ID);
    }

    @Test
    void fetchPayment_razorpayFails_propagatesSameRazorpayException() throws RazorpayException {

        // Arrange
        RazorpayException razorpayException = org.mockito.Mockito.mock(RazorpayException.class);

        when(paymentClient.fetch(PAYMENT_ID)).thenThrow(razorpayException);

        // Act
        RazorpayException exception = assertThrows(RazorpayException.class, () -> razorpayService.fetchPayment(PAYMENT_ID));

        // Assert
        assertSame(razorpayException, exception);

        verify(paymentClient).fetch(PAYMENT_ID);
    }


    // -------------------------------------------------------------------------
    // Helpers
    // -------------------------------------------------------------------------

    private String generateSignature(String orderId, String paymentId, String secret) throws Exception {

        String payload = orderId + "|" + paymentId;

        Mac mac = Mac.getInstance("HmacSHA256");

        SecretKeySpec secretKey = new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256");

        mac.init(secretKey);

        byte[] hash = mac.doFinal(payload.getBytes(StandardCharsets.UTF_8));

        return HexFormat.of().formatHex(hash);
    }
}