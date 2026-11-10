package dev.mostapha.bidmaster;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import dev.mostapha.bidmaster.application.port.in.AuctionUseCase;
import dev.mostapha.bidmaster.application.port.in.UserUseCase;
import dev.mostapha.bidmaster.application.port.out.SecurityScanPort;
import dev.mostapha.bidmaster.config.JwtService;
import dev.mostapha.bidmaster.domain.model.auction.Auction;
import dev.mostapha.bidmaster.domain.model.auction.AuctionCategory;
import dev.mostapha.bidmaster.domain.model.auction.AuctionStatus;
import dev.mostapha.bidmaster.domain.model.bid.Bid;
import dev.mostapha.bidmaster.domain.model.bid.BidStatus;
import dev.mostapha.bidmaster.domain.model.user.User;
import dev.mostapha.bidmaster.domain.model.user.UserRole;

@SpringBootTest
@ActiveProfiles("test")
class BidMasterApplicationTests {

	@Autowired
	private UserUseCase userUseCase;

	@Autowired
	private AuctionUseCase auctionUseCase;

	@Autowired
	private JwtService jwtService;

	@Autowired
	private SecurityScanPort securityScanPort;

	@Test
	void contextLoadsAndFullAuctionBidFlowWorks() {
		// 1. Registrar vendedor y dos compradores con BCrypt
		User seller = userUseCase.registerUser(
				User.create("seller_test", "seller@test.com", "Secret123!", UserRole.USER));
		User buyer1 = userUseCase.registerUser(
				User.create("buyer1_test", "buyer1@test.com", "Secret123!", UserRole.USER));
		User buyer2 = userUseCase.registerUser(
				User.create("buyer2_test", "buyer2@test.com", "Secret123!", UserRole.USER));

		userUseCase.addBalance(buyer1.getId(), new BigDecimal("1000.00"));
		userUseCase.addBalance(buyer2.getId(), new BigDecimal("1000.00"));

		// 2. Verificar login y emisión de JWT real
		User loggedIn = userUseCase.login("buyer1_test", "Secret123!");
		String token = jwtService.generateToken(loggedIn);
		assertThat(token).isNotBlank();
		assertThat(jwtService.isTokenValid(token)).isTrue();
		assertThat(jwtService.extractUsername(token)).isEqualTo("buyer1_test");

		// 3. Crear subasta con depósito requerido y publicarla
		Auction draft = Auction.create(
				"Cámara Leica M6 Clásica",
				"Cámara telemétrica analógica Leica M6 en perfecto estado mecánico y óptico.",
				AuctionCategory.COLLECTIBLES,
				new BigDecimal("500.00"),
				LocalDateTime.now().plusDays(3),
				seller.getId());
		draft.setDepositRequired(new BigDecimal("50.00"));

		Auction created = auctionUseCase.createAuction(draft);
		assertThat(created.getStatus()).isEqualTo(AuctionStatus.DRAFT);

		Auction active = auctionUseCase.publishAuction(created.getId());
		assertThat(active.getStatus()).isEqualTo(AuctionStatus.ACTIVE);

		// 4. Primera puja (buyer1) -> bloquea depósito de 50.00
		Auction afterBid1 = auctionUseCase.placeBid(active.getId(), buyer1.getId(), new BigDecimal("520.00"));
		assertThat(afterBid1.getCurrentPrice()).isEqualByComparingTo("520.00");
		assertThat(afterBid1.getWinnerId()).isEqualTo(buyer1.getId());
		assertThat(afterBid1.getTotalBids()).isEqualTo(1);

		User buyer1AfterBid1 = userUseCase.findUserById(buyer1.getId()).orElseThrow();
		assertThat(buyer1AfterBid1.getBalance()).isEqualByComparingTo("950.00");
		assertThat(buyer1AfterBid1.getBlockedBalance()).isEqualByComparingTo("50.00");

		// 5. Segunda puja superadora (buyer2) -> desbloquea depósito de buyer1 y bloquea a buyer2
		Auction afterBid2 = auctionUseCase.placeBid(active.getId(), buyer2.getId(), new BigDecimal("550.00"));
		assertThat(afterBid2.getCurrentPrice()).isEqualByComparingTo("550.00");
		assertThat(afterBid2.getWinnerId()).isEqualTo(buyer2.getId());
		assertThat(afterBid2.getTotalBids()).isEqualTo(2);

		User buyer1AfterOutbid = userUseCase.findUserById(buyer1.getId()).orElseThrow();
		assertThat(buyer1AfterOutbid.getBalance()).isEqualByComparingTo("1000.00");
		assertThat(buyer1AfterOutbid.getBlockedBalance()).isEqualByComparingTo("0.00");

		User buyer2AfterBid = userUseCase.findUserById(buyer2.getId()).orElseThrow();
		assertThat(buyer2AfterBid.getBalance()).isEqualByComparingTo("950.00");
		assertThat(buyer2AfterBid.getBlockedBalance()).isEqualByComparingTo("50.00");

		// 6. Verificar historial de pujas persistido en tabla bids
		List<Bid> bids = auctionUseCase.findBidsByAuctionId(active.getId());
		assertThat(bids).hasSize(2);
		assertThat(bids).anyMatch(b -> b.getBidderId().equals(buyer2.getId()) && b.getStatus() == BidStatus.WINNING);
		assertThat(bids).anyMatch(b -> b.getBidderId().equals(buyer1.getId()) && b.getStatus() == BidStatus.OUTBID);
	}

	@Test
	void magicBytesSecurityValidationDetectsSpoofedFiles() {
		byte[] fakeImage = "<?php echo 'hack'; ?>".getBytes();
		assertThat(securityScanPort.validateFileType(fakeImage, "image/jpeg")).isFalse();
	}
}
