package server;

import external.diff_match_patch;
import models.Message;
import org.junit.Test;

import java.util.Date;

import static org.junit.Assert.assertEquals;

public class StatesTest {

    private diff_match_patch patcher = new diff_match_patch();

    @Test
    public void diffMerge() {
        States states = new States();

        String text1 = "lorem dolor and amet";
        String text2 = "ipsum dolor sit amet";

        assertEquals("lorem ipsum dolor and sit amet", states.diffMerge(text1, text2));
    }

    @Test
    public void computeState() {
        States states = new States();

        String text1 = "lorem ipsum dolor sit amet";
        Message message = new Message(patcher.patch_make("", text1), "TestAuthor", 0);
        states.append(message);

        String text2 = "lorem ipsum sequi sit amet";
        message = new Message(patcher.patch_make(text1, text2), "TestAuthor", 1);
        states.append(message);

        assertEquals("lorem ipsum sequi sit amet", states.computeState(2));
    }

    @Test
    public void append() {
        States states = new States();

        String text1 = "lorem ipsum dolor sit amet";
        Message message = new Message(patcher.patch_make("", text1), "TestAuthor", 0);
        states.append(message);

        // all clients updated to state 1, then

        String text2 = "lorem ipsum sequi sit amet";
        message = new Message(patcher.patch_make(text1, text2), "TestAuthor", 1);
        states.append(message);

        // all clients updated to state 2, then

        String text3 = "lorem ipsum sequi sit amet and";
        message = new Message(patcher.patch_make(text2, text3), "TestAuthor", 2);
        states.append(message);

        // client isn't updated to state 3, then he sends

        String text4 = "lorem ipsum sequi sit amet or";
        message = new Message(patcher.patch_make(text2, text4), "TestAuthor", 1);
        states.append(message);

        // so States creates state 4 to fix it

        Message expected = new Message(patcher.patch_make(text3, "lorem ipsum sequidolor sit amet andor"), "TestAuthor", 4);
        expected.setDate(new Date(0));
        states.getLast().setDate(new Date(0));

        assertEquals(expected, states.getLast());
    }
}