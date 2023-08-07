package com.jpswcons.auctioneer;

import io.micrometer.core.aop.TimedAspect;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.composite.CompositeMeterRegistry;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.retry.annotation.EnableRetry;

@SpringBootApplication
@EnableRetry
public class AuctioneerApplication {

	public static void main(String[] args) {
		SpringApplication.run(AuctioneerApplication.class, args);
	}

	@Bean
	public MeterRegistry getMeterRegistry() {
		return new CompositeMeterRegistry();
	}

	@Bean
	public TimedAspect timedAspect(MeterRegistry registry) {
		return new TimedAspect(registry);
	}

}
