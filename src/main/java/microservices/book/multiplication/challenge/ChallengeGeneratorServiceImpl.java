package microservices.book.multiplication.challenge;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import microservices.book.multiplication.serviceclients.GamificationServiceClient;
import microservices.book.multiplication.user.User;
import microservices.book.multiplication.user.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Random;

@Slf4j
@RequiredArgsConstructor
@Service
public class ChallengeGeneratorServiceImpl implements ChallengeGeneratorService{
    private final UserRepository userRepository;
    private final ChallengeAttemptRepository attemptRepository;
    private final GamificationServiceClient gameClient;
    private final Random random;
    private final static int MINIMUM_FACTOR = 11;
    private final static int MAXIMUM_FACTOR = 100;

   // ChallengeGeneratorServiceImpl() {
  //      this.random = new Random();
  //  }

   // protected ChallengeGeneratorServiceImpl(final Random random) {
   //     this.random = random;
  //  }

    private int next() {
        return random.nextInt(MAXIMUM_FACTOR - MINIMUM_FACTOR) + MINIMUM_FACTOR;
    }

    @Override
    public Challenge randomChallenge() {
        return new Challenge(next(), next());
    }

    @Override
    public ChallengeAttempt verifyAttempt(ChallengeAttemptDTO attemptDTO) {

        // check if user exist else create one
        User user = userRepository.findByAlias(attemptDTO.getName())
                .orElseGet(() -> {
                    log.info("Creating user with alias {}",
                            attemptDTO.getName());
                    return userRepository.save(new User(attemptDTO.getName()));
                });
        // check if attempt is correct
        boolean isCorrect = attemptDTO.getGuess() == attemptDTO.getFactorA() * attemptDTO.getFactorB();

        // build domain object

        ChallengeAttempt checkedAttempt = new ChallengeAttempt(null,
                user,
                attemptDTO.getFactorA(),
                attemptDTO.getFactorB(),
                attemptDTO.getGuess(),
                isCorrect);
        //Stores attempt
        ChallengeAttempt storedAttempt = attemptRepository.save(checkedAttempt);

        // send attempt to gamification
        boolean status = gameClient.sendAttempt(storedAttempt);
        log.info("Gamification service response: {}", status);
        return storedAttempt;
    }

    @Override
    public List<ChallengeAttempt> getStatsForUser(final String userAlias) {
        return attemptRepository.findTop10ByUserAliasOrderByIdDesc(userAlias);
    }
}
