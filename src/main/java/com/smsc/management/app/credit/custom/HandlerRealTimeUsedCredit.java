package com.smsc.management.app.credit.custom;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;

import com.paicbd.smsc.utils.GeneralSmscConstants;
import com.smsc.management.utils.AppProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import com.paicbd.smsc.utils.RedisManager;

@Component
@RequiredArgsConstructor
@Slf4j
public class HandlerRealTimeUsedCredit {
	private static final Logger prometheusLogger = LoggerFactory.getLogger("prometheus");
	private final HandlerServiceProvider handlerSp;

	/** Map to store the instances properties */
	private final HandlerCreditByServiceProvider handlerCreditUsed;
	private final RedisManager redisManager;
	
	// for the execution of the new thread
	private final ThreadFactory factory = r -> new Thread(r, "HandlerRealTimeValuesThread");
    private final ScheduledExecutorService deliveryExecService = Executors.newScheduledThreadPool(0, factory);
    private  final AppProperties appProperties;

	@PostConstruct
	public void init() {
		// Calculate initial delay
        long initialDelay = appProperties.getBalancePeriodToStore() - (System.currentTimeMillis() % appProperties.getBalancePeriodToStore());
        
        // Schedule the recurring task with the calculated interval and initial delay
        deliveryExecService.scheduleAtFixedRate(updateAndResetRatingPerSecond(), initialDelay, appProperties.getBalancePeriodToStore(), TimeUnit.SECONDS);
	}
	
	public Runnable updateAndResetRatingPerSecond() {
		return () ->
			handlerCreditUsed.getCurrentBalanceMap().forEach((key, value) -> {
				Long lastBalance = handlerCreditUsed.getLastBalance(key);

				// handler TPS : try catch to handle division by zero
				long tps = 0L;
				try {
					Long currentBalance = value.get();
					/*
					 * If the current balance is negative, it indicates that credits have been overused.
					 * In this case, the consumed credits include the last balance plus the negative count.
					 * For example:
					 * If lastBalance = 30 and currentBalance = -20,
					 * then the total consumed credits would be 50.
					 */
					if (currentBalance < 0) {
						lastBalance = lastBalance + (-currentBalance * 2);
						currentBalance = -currentBalance; // convert to a positive number
					}
					
					tps = (lastBalance - currentBalance) / appProperties.getBalancePeriodToStore();
				} catch (Exception e) {
					log.error("Error to calculate TPS -> {}", e.getMessage());
				}

				// store in Redis and database
				String balanceStr = "{\"balance\": " + value.get() + ",\"tps\": " + Math.max(0, tps) + "}";
				redisManager.hset(GeneralSmscConstants.BALANCE_HANDLER_HASH_NAME, String.valueOf(key), balanceStr);
				if (value.get() != handlerCreditUsed.getLastBalance(key)) {
					handlerCreditUsed.updateDataBase(key, value.get());
				}
				
				// update lastBalance to handler tps
				if (tps > 0) {
					handlerCreditUsed.setLastBalance(key, value.get());
				}
				
				// logger prometheus
				try {
					String protocol = handlerSp.getConfigForClient(key).getProtocol();
					prometheusLogger.info("submit_sm|{}|{}|{}", protocol, key, tps);
				} catch (Exception e) {
                    log.error("Error to get service provider config to print prometheus log -> {}", e.getMessage());
				}
			});
	}
}
