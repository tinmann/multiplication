package microservices.book.multiplication.challenge;

import java.util.List;

public interface ChallengeGeneratorService {
    Challenge randomChallenge();

    ChallengeAttempt verifyAttempt(ChallengeAttemptDTO resultAttempt);

    List<ChallengeAttempt> getStatsForUser(String userAlias);
}
