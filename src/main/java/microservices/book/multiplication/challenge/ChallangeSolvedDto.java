package microservices.book.multiplication.challenge;

import lombok.Value;

@Value
public class ChallangeSolvedDto {
    long attemptId;
    boolean correct;
    int factorA;
    int factorB;
    long userId;
    String userAlias;
}
