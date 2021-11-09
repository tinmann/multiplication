package microservices.book.multiplication.challenge;

import microservices.book.multiplication.serviceclients.GamificationServiceClient;
import microservices.book.multiplication.user.User;
import microservices.book.multiplication.user.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import static org.mockito.AdditionalAnswers.returnsFirstArg;
import static org.mockito.Matchers.any;

import static org.mockito.BDDMockito.given;
import static org.assertj.core.api.BDDAssertions.then;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import java.util.Optional;
import java.util.Random;

@ExtendWith(MockitoExtension.class)
public class ChallengeGeneratorServiceTest {

    private ChallengeGeneratorService challengeGeneratorService;

    @Mock
    private UserRepository userRepository;

    @Mock
    private ChallengeAttemptRepository attemptRepository;

    @Mock
    private GamificationServiceClient gameClient;

    @Spy
    private Random random;

    @BeforeEach
    public void setup() {
        challengeGeneratorService = new ChallengeGeneratorServiceImpl(
                userRepository,
                attemptRepository,
                gameClient,
                random
        );
        given(attemptRepository.save(any()))
                .will(returnsFirstArg());
    }

    //@Test
    public void generateRandomFactorWithinExpectedLimits() {
        given(random.nextInt(89)).willReturn(20, 30);

        Challenge challenge = challengeGeneratorService.randomChallenge();

        then(challenge).isEqualTo(new Challenge(31, 41));
    }

    @Test
    public void checkCorrectAttemptTest() {
        ChallengeAttemptDTO challengeAttemptDTO = new ChallengeAttemptDTO(50, 60, "NotSarigam", 3000);

        ChallengeAttempt resultAttempt = challengeGeneratorService.verifyAttempt(challengeAttemptDTO);

        then(resultAttempt.isCorrect()).isTrue();

        //newely added
        verify(userRepository).save(new User("NotSarigam"));
        verify(attemptRepository).save(resultAttempt);
       // verify(gameClient).sendAttempt(resultAttempt);
    }

    @Test
    public void checkWrongAttemptTest() {
        ChallengeAttemptDTO challengeAttemptDTO = new ChallengeAttemptDTO(50, 60, "sarigam", 5000);

        ChallengeAttempt resultAttempt = challengeGeneratorService.verifyAttempt(challengeAttemptDTO);

        then(resultAttempt.isCorrect()).isFalse();
    }

    @Test
    public void checkExistingUserTest() {
        User existingUser = new User(1L, "NotSarigam");
        given(userRepository.findByAlias("NotSarigam"))
                .willReturn(Optional.of(existingUser));
        ChallengeAttemptDTO attemptDTO = new ChallengeAttemptDTO(50, 60, "NotSarigam", 5000);

        ChallengeAttempt resultAttempt = challengeGeneratorService.verifyAttempt(attemptDTO);

        then(resultAttempt.isCorrect()).isFalse();
        then(resultAttempt.getUser()).isEqualTo(existingUser);
        verify(userRepository, never()).save(any());
        verify(attemptRepository).save(resultAttempt);
        verify(gameClient).sendAttempt(resultAttempt);
    }
}
