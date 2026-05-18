package org.pgsg.product.application.service;

import static org.pgsg.product.global.exception.ProductErrorCode.*;

import java.util.UUID;

import org.pgsg.common.event.OutboxEvent;
import org.pgsg.common.exception.CustomException;
import org.pgsg.common.util.SecurityUtil;
import org.pgsg.config.security.UserDetailsImpl;
import org.pgsg.product.application.dto.command.CreateProductCommand;
import org.pgsg.product.application.dto.command.UpdateProductCommand;
import org.pgsg.product.application.dto.command.UpdateTimeDealCommand;
import org.pgsg.product.application.dto.result.CreateProductResult;
import org.pgsg.product.application.dto.result.UpdateProductResult;
import org.pgsg.product.domain.event.ProductCreatedEvent;
import org.pgsg.product.domain.event.TimeDealScheduleEvent;
import org.pgsg.product.domain.model.Product;
import org.pgsg.product.domain.model.TimeDealSchedule;
import org.pgsg.product.domain.repository.ProductRepository;
import org.pgsg.product.infrastructure.scheduler.TimeDealSchedulerService;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/*
* 상품 추가, 삭제, 수정 조회, 이벤트 연결
* */
@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class ProductCommandService {
	private final ProductRepository productRepository;
	private final TimeDealSchedulerService schedulerService;
	private final ApplicationEventPublisher eventPublisher;

	/*
	* 상품 등록
	* */
	public CreateProductResult createProduct(CreateProductCommand command) {
		Product product =Product.create(
			command.name(),command.price(),command.description(),command.startTime(),command.endTime());

		Product saved=productRepository.save(product);
		log.info("Created product: productId:{}, userId:{}", saved.getId(),saved.getCreatedBy());

		eventPublisher.publishEvent(
			new TimeDealScheduleEvent(saved.getId(), saved.getTimeDealSchedule().getStartTime())
		);

		return new CreateProductResult(saved.getId(),saved.getName(),saved.getPrice(),saved.getDescription());
	}

	/*
	* 상품 정보 수정
	* */
	public UpdateProductResult updateProduct(UUID id, UpdateProductCommand command) {
		Product product = findById(id);
		checkAuthorization(product.getCreatedBy());

		product.updateInfo(command.name(), command.price(), command.description());

		Product saved = productRepository.saveAndFlush(product);
		log.info("Updated product: productId:{}, userId:{}", saved.getId(), saved.getModifiedBy());

		TimeDealSchedule schedule = saved.getTimeDealSchedule();
		return new UpdateProductResult(
			saved.getName(),
			saved.getPrice(),
			saved.getDescription(),
			schedule == null ? null : schedule.getStartTime(),
			schedule == null ? null : schedule.getEndTime()
		);
	}

	/*
	* 타임딜 수정
	* */
	public UpdateProductResult updateTimeDeal(UUID id, UpdateTimeDealCommand command) {
		Product product = findById(id);
		checkAuthorization(product.getCreatedBy());

		product.updateTimeDeal(command.startTime(),command.endTime());

		Product saved = productRepository.saveAndFlush(product);
		log.info("타임딜 수정: productId={}, userId={}", saved.getId(), saved.getModifiedBy());

		eventPublisher.publishEvent(
			new TimeDealScheduleEvent(saved.getId(), saved.getTimeDealSchedule().getStartTime())
		);

		return new UpdateProductResult(saved.getName(), saved.getPrice(), saved.getDescription(),
			saved.getTimeDealSchedule().getStartTime(), saved.getTimeDealSchedule().getEndTime());
	}


	/*
	* 판매 취소 / 삭제
	* */
	public void cancelSale(UUID id) {
		Product product = findById(id);
		checkAuthorization(product.getCreatedBy());
		product.cancelSale();
		schedulerService.cancelSchedule(id);
		log.info("판매 취소: productId={}", id);
	}

	public void deleteProduct(UUID id) {
		Product product=findById(id);
		checkAuthorization(product.getCreatedBy());

		UUID userId = SecurityUtil.getCurrentUserIdOrThrow();
		product.deleteProduct(userId);
		schedulerService.cancelSchedule(id);
		log.info("상품 삭제. productId:{}, userId:{}", id, userId);
	}

	/*
	* 외부 이벤트 수신 핸들러
	* */

	public void completeTrade(UUID id) {
		Product product = findById(id);
		product.complete();
		log.info("거래 완료. productId:{}", product.getId());
	}

	public void completeReservation(UUID id) {
		Product product = findById(id);
		product.startTrade();
		log.info("거래 시작. productId:{}", product.getId());
	}

	public void pendingSale(UUID id) {
		Product product = findById(id);
		product.revertToReserving();
		log.info("판매 대기 전환. productId:{}", product.getId());
	}

	public void revertToReserving(UUID id) {
		Product product = findById(id);
		product.revertToReserving();
		log.info("예약 진행 복귀: productId={}", id);
	}


	/*
	* 내부 헬퍼
	* */
	private Product findById(UUID productId) {
		return productRepository.findById(productId)
			.orElseThrow(()->new CustomException(ProductNotFoundException));
	}

	private void checkAuthorization(UUID sellerId) {
		String userRole=SecurityUtil.getCurrentUser()
			.map(UserDetailsImpl::getUserRole)
			.orElse(null);
		if(userRole==null)
			throw new CustomException(Forbidden);
		else if(userRole.equals("USER")) {
			if(!SecurityUtil.getCurrentUserIdOrThrow().equals(sellerId))
				throw new CustomException(Unauthorized);
		}
	}
}
