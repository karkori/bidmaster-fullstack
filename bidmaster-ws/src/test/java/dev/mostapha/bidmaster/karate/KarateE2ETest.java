package dev.mostapha.bidmaster.karate;

import com.intuit.karate.junit5.Karate;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class KarateE2ETest {

    @LocalServerPort
    private int port;

    @Karate.Test
    Karate testUsersAndAuth() {
        return Karate.run("classpath:karate/users-auth.feature")
                .systemProperty("karate.server.port", String.valueOf(port));
    }

    @Karate.Test
    Karate testAuctionsLifecycle() {
        return Karate.run("classpath:karate/auctions-lifecycle.feature")
                .systemProperty("karate.server.port", String.valueOf(port));
    }

    @Karate.Test
    Karate testBiddingRules() {
        return Karate.run("classpath:karate/bidding-rules.feature")
                .systemProperty("karate.server.port", String.valueOf(port));
    }

    @Karate.Test
    Karate testImagesSecurity() {
        return Karate.run("classpath:karate/images-security.feature")
                .systemProperty("karate.server.port", String.valueOf(port));
    }
}
