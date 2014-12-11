/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.freedomotic.core;

import com.freedomotic.nlp.Nlp;
import com.freedomotic.nlp.NlpCommand;
import com.freedomotic.reactions.Command;
import com.freedomotic.reactions.CommandRepository;
import com.freedomotic.testutils.FreedomoticTestsInjector;
import com.freedomotic.testutils.GuiceJUnitRunner;
import java.util.List;
import javax.inject.Inject;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;
import org.junit.runner.RunWith;

/**
 *
 * @author enrico
 */
@RunWith(GuiceJUnitRunner.class)
@GuiceJUnitRunner.GuiceInjectors({FreedomoticTestsInjector.class})
public class FreeFormCommandsInterpreterTest {

    @Inject
    CommandRepository commandRepository;
    @Inject
    NlpCommand nlpCommand;

    public FreeFormCommandsInterpreterTest() {
    }

    @BeforeClass
    public static void setUpClass() {
    }

    @AfterClass
    public static void tearDownClass() {
    }

    @Before
    public void setUp() {
        Command expectedResult = new Command();
        expectedResult.setName("Turn on kitchen light");
        expectedResult.setReceiver("app.events.sensors.behavior.request.objects");
        expectedResult.setProperty("object", "Kitchen Light");
        expectedResult.setProperty("behavior", "powered");
        expectedResult.setProperty("value", "true");
        commandRepository.create(expectedResult);
    }

    @After
    public void tearDown() {
        commandRepository.deleteAll();
    }

    /**
     * Test of findMostSimilarCommand method, of class
     * FreeFormCommandsInterpreter.
     */
    @Test
    public void testFindMostSimilarCommand() throws Exception {
        System.out.println("Find the most similar command using NLP");

        String phrase = "kitccchen LiGhTT Power ON";
        // Compute the commands ranking
        List<Nlp.Rank<Command>> ranking = nlpCommand.computeSimilarity(phrase, 10);
        assertEquals("Should find exacly one similar command", ranking.size(), 1);
    }

    @Test
    public void testNoSimilarCommand() throws Exception {
        System.out.println("No similar command exists");

        String phrase = "asdasd tretert gbffdg uyututy mnbb";
        // Compute the commands ranking
        List<Nlp.Rank<Command>> ranking = nlpCommand.computeSimilarity(phrase, 10);
        assertEquals("Should find a command anyway, because zero similarity is allowed", ranking.size(), 1);
    }

}
