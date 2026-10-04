package com.nbsh.commerceapi.observability;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.springframework.stereotype.Component;

import java.time.Duration;

@Component
public class CommerceMetrics {

    private final MeterRegistry meterRegistry;

    public CommerceMetrics(
            MeterRegistry meterRegistry
    ) {
        this.meterRegistry =
                meterRegistry;
    }

    public void recordCheckoutSuccess(
            Duration duration
    ) {

        Counter.builder(
                        "commerce.checkout"
                )
                .description(
                        "Checkout attempts"
                )
                .tag(
                        "result",
                        "success"
                )
                .register(meterRegistry)
                .increment();

        Timer.builder(
                        "commerce.checkout.duration"
                )
                .description(
                        "Checkout duration"
                )
                .tag(
                        "result",
                        "success"
                )
                .register(meterRegistry)
                .record(duration);
    }

    public void recordCheckoutFailure(
            String reason,
            Duration duration
    ) {

        Counter.builder(
                        "commerce.checkout"
                )
                .description(
                        "Checkout attempts"
                )
                .tag(
                        "result",
                        "failure"
                )
                .tag(
                        "reason",
                        reason
                )
                .register(meterRegistry)
                .increment();

        Timer.builder(
                        "commerce.checkout.duration"
                )
                .description(
                        "Checkout duration"
                )
                .tag(
                        "result",
                        "failure"
                )
                .register(meterRegistry)
                .record(duration);
    }

    public void recordPaymentResult(
            String result
    ) {

        Counter.builder(
                        "commerce.payment"
                )
                .description(
                        "Payment results"
                )
                .tag(
                        "result",
                        result
                )
                .register(meterRegistry)
                .increment();
    }

    public void recordPaymentGatewayDuration(
            String outcome,
            Duration duration
    ) {

        Timer.builder(
                        "commerce.payment.gateway.duration"
                )
                .description(
                        "External payment gateway duration"
                )
                .tag(
                        "outcome",
                        outcome
                )
                .register(meterRegistry)
                .record(duration);
    }
}