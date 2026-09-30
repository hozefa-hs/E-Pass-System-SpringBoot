package com.porfolio.EPassSystemSpringboot.services.implementations;

import com.porfolio.EPassSystemSpringboot.dtos.PaymentResponseDto;
import com.porfolio.EPassSystemSpringboot.entities.PassApplication;
import com.porfolio.EPassSystemSpringboot.entities.Payment;
import com.porfolio.EPassSystemSpringboot.enums.ApplicationStatus;
import com.porfolio.EPassSystemSpringboot.enums.PassStatus;
import com.porfolio.EPassSystemSpringboot.enums.PaymentStatus;
import com.porfolio.EPassSystemSpringboot.exceptions.BusinessException;
import com.porfolio.EPassSystemSpringboot.exceptions.ResourceNotFoundException;
import com.porfolio.EPassSystemSpringboot.repositories.PassApplicationRepository;
import com.porfolio.EPassSystemSpringboot.repositories.PassRepository;
import com.porfolio.EPassSystemSpringboot.repositories.PaymentRepository;
import com.porfolio.EPassSystemSpringboot.services.PassPriceService;
import com.porfolio.EPassSystemSpringboot.services.PassService;
import com.porfolio.EPassSystemSpringboot.services.PaymentService;
import com.porfolio.EPassSystemSpringboot.services.RazorpayService;
import com.razorpay.RazorpayException;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PaymentServiceImpl implements PaymentService {

    private final ModelMapper modelMapper;
    private final PassApplicationRepository passApplicationRepository;
    private final PassRepository passRepository;
    private final PaymentRepository paymentRepository;
    private final PassPriceService passPriceService;
    private final PassService passService;
    private final RazorpayService razorpayService;


    @Override
    @Transactional
    public PaymentResponseDto initiatePayment(Long applicationId) throws RazorpayException {

        PassApplication passApplication = passApplicationRepository.findById(applicationId).orElseThrow(() -> new ResourceNotFoundException("Pass application not found"));

        if (passApplication.getApplicationStatus() != ApplicationStatus.APPROVED) {
            throw new BusinessException("Payment can only be initiated for an approved application");
        }

        if (passRepository.existsByUserAndPassStatus(passApplication.getPassenger(), PassStatus.ACTIVE)) {
            throw new BusinessException("Passenger already has an active pass");
        }

        if (paymentRepository.existsByPassApplicationApplicationIdAndPaymentStatus(applicationId, PaymentStatus.SUCCESS)) {
            throw new BusinessException("Payment has already been completed for this application");
        }


        BigDecimal amount = passPriceService.getPrice(passApplication.getPassType(), passApplication.getPassValidity());

        Payment payment = Payment.builder().passApplication(passApplication).amount(amount).paymentStatus(PaymentStatus.PENDING).initiatedAt(LocalDateTime.now()).build();

        Payment savedPayment = paymentRepository.save(payment);

        String razorpayOrderId = razorpayService.createOrder(amount, passApplication.getApplicationNumber());

        savedPayment.setGatewayOrderId(razorpayOrderId);

        savedPayment = paymentRepository.save(savedPayment);

        return modelMapper.map(savedPayment, PaymentResponseDto.class);
    }

    @Override
    public PaymentResponseDto verifyAndCompletePayment(Long paymentId, String razorpayOrderId, String razorpayPaymentId, String razorpaySignature) {

        //1. Payment exists
        //2. Payment is PENDING
        //3. Stored gatewayOrderId == received razorpayOrderId
        //4. Razorpay signature is valid
        //5. Mark payment SUCCESS
        //6. Store razorpayPaymentId
        //7. Set paidAt
        //8. Call issuePass(applicationId)

        return new PaymentResponseDto();
    }

    @Override
    @Transactional
    public PaymentResponseDto markPaymentSuccess(Long paymentId, String gatewayPaymentId) {

        Payment payment = paymentRepository.findById(paymentId).orElseThrow(() -> new ResourceNotFoundException("Payment not found"));

        if (payment.getPaymentStatus() == PaymentStatus.SUCCESS) {
            return modelMapper.map(payment, PaymentResponseDto.class);
        }

        if (payment.getPaymentStatus() != PaymentStatus.PENDING) {
            throw new BusinessException("Only pending payments can be marked as successful");
        }

        payment.setPaymentStatus(PaymentStatus.SUCCESS);
        payment.setGatewayPaymentId(gatewayPaymentId);
        payment.setPaidAt(LocalDateTime.now());

        Payment savedPayment = paymentRepository.save(payment);

        //issue pass with status active
        passService.issuePass(payment.getPassApplication().getApplicationId());

        return modelMapper.map(savedPayment, PaymentResponseDto.class);
    }

    @Override
    @Transactional
    public PaymentResponseDto markPaymentFailed(Long paymentId) {
        Payment payment = paymentRepository.findById(paymentId).orElseThrow(() -> new ResourceNotFoundException("Payment not found"));

        if (payment.getPaymentStatus() != PaymentStatus.PENDING) {
            throw new BusinessException("Only pending payments can be marked as failed");
        }

        payment.setPaymentStatus(PaymentStatus.FAILED);

        Payment savedPayment = paymentRepository.save(payment);

        return modelMapper.map(savedPayment, PaymentResponseDto.class);
    }

    @Override
    public PaymentResponseDto getOwnPayment(Long paymentId) {
        Payment payment = paymentRepository.findById(paymentId).orElseThrow(() -> new ResourceNotFoundException("Payment not found"));
        return modelMapper.map(payment, PaymentResponseDto.class);
    }

    @Override
    public List<PaymentResponseDto> getAllPaymentsByApplication(Long applicationId) {

        PassApplication passApplication = passApplicationRepository.findById(applicationId).orElseThrow(() -> new ResourceNotFoundException("Pass application not found"));

        List<Payment> paymentList = paymentRepository.findAllByPassApplicationApplicationIdOrderByInitiatedAtDesc(applicationId);

        return paymentList
                .stream()
                .map(payment -> modelMapper.map(payment, PaymentResponseDto.class))
                .toList();
    }

}
