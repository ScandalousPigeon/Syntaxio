package com.example.syntaxio.database;

import com.example.syntaxio.model.InProgressChallenge;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SqliteInProgressChallengeDAOTest {
    private static final int USER_ID = 1;
    private static final String CHALLENGE_ID = "ch-001";
    private static final String FIRST_DRAFT = "return 0;";
    private static final String UPDATED_DRAFT = "return sum;";

    @TempDir
    Path tempDir;

    @BeforeEach
    void setUp() {
        SqliteConnection.closeConnection();
        System.setProperty(
                SqliteConnection.DATABASE_PATH_PROPERTY,
                tempDir.resolve("syntaxio-test.db").toString()
        );
    }

    @AfterEach
    void tearDown() {
        SqliteConnection.closeConnection();
        System.clearProperty(SqliteConnection.DATABASE_PATH_PROPERTY);
    }

    @Test
    void saveOrUpdateInProgressCreatesAndUpdatesDraft() {
        SqliteInProgressChallengeDAO dao = new SqliteInProgressChallengeDAO();

        assertTrue(dao.saveOrUpdateInProgress(USER_ID, CHALLENGE_ID, FIRST_DRAFT));
        Optional<InProgressChallenge> firstSaved = dao.getInProgressForUserAndChallenge(USER_ID, CHALLENGE_ID);
        assertTrue(firstSaved.isPresent());

        assertTrue(dao.saveOrUpdateInProgress(USER_ID, CHALLENGE_ID, UPDATED_DRAFT));
        Optional<InProgressChallenge> updated = dao.getInProgressForUserAndChallenge(USER_ID, CHALLENGE_ID);

        assertTrue(updated.isPresent());
        assertAll(
                () -> assertEquals(USER_ID, updated.get().getUserId()),
                () -> assertEquals(CHALLENGE_ID, updated.get().getChallengeId()),
                () -> assertEquals(UPDATED_DRAFT, updated.get().getDraftCode()),
                () -> assertEquals(firstSaved.get().getStartedAt(), updated.get().getStartedAt()),
                () -> assertFalse(updated.get().getUpdatedAt().isBefore(firstSaved.get().getUpdatedAt()))
        );
    }

    @Test
    void getInProgressForUserReturnsNewestFirst() throws InterruptedException {
        SqliteInProgressChallengeDAO dao = new SqliteInProgressChallengeDAO();

        assertTrue(dao.saveOrUpdateInProgress(USER_ID, "ch-001", "first"));
        Thread.sleep(5);
        assertTrue(dao.saveOrUpdateInProgress(USER_ID, "ch-002", "second"));

        List<InProgressChallenge> challenges = dao.getInProgressForUser(USER_ID);

        assertEquals(2, challenges.size());
        assertEquals("ch-002", challenges.get(0).getChallengeId());
        assertEquals("ch-001", challenges.get(1).getChallengeId());
    }

    @Test
    void removeInProgressDeletesOnlyMatchingChallenge() {
        SqliteInProgressChallengeDAO dao = new SqliteInProgressChallengeDAO();

        assertTrue(dao.saveOrUpdateInProgress(USER_ID, "ch-001", "first"));
        assertTrue(dao.saveOrUpdateInProgress(USER_ID, "ch-002", "second"));

        assertTrue(dao.removeInProgress(USER_ID, "ch-001"));

        assertTrue(dao.getInProgressForUserAndChallenge(USER_ID, "ch-001").isEmpty());
        assertTrue(dao.getInProgressForUserAndChallenge(USER_ID, "ch-002").isPresent());
    }
}
