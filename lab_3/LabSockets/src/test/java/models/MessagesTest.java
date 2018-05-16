package models;

import external.diff_match_patch;
import org.junit.Before;
import org.junit.Test;

import java.util.Date;
import java.util.LinkedList;

import static org.junit.Assert.*;

public class MessagesTest {

    private diff_match_patch dmp;

    @Before
    public void setUp() throws Exception {
        dmp = new diff_match_patch();
    }

    @Test
    public void toMessage() {
        String json = "{\"patches\":[{\"diffs\":[{\"operation\":\"EQUAL\",\"text\":\"sum \"},{\"operation\":\"DELETE\",\"text\":\"dolor\"},{\"operation\":\"INSERT\",\"text\":\"sequi\"},{\"operation\":\"EQUAL\",\"text\":\" sit\"}],\"start1\":8,\"start2\":8,\"length1\":13,\"length2\":13}],\"author\":\"TestAuthor\",\"date\":4321,\"stateId\":1234}";

        String text1 = "lorem ipsum dolor sit ame";
        String text2 = "lorem ipsum sequi sit ame";

        LinkedList<diff_match_patch.Diff> diffs = dmp.diff_main(text1, text2);
        dmp.diff_cleanupEfficiency(diffs);
        LinkedList<diff_match_patch.Patch> patches = dmp.patch_make(text1, diffs);
        Message expected = new Message(patches, "TestAuthor", 1234);
        expected.setDate(new Date(4321));

        assertEquals(expected, Messages.toMessage(Messages.toJsonNode(json)));
    }

    @Test
    public void toJson() {
        String text1 = "lorem ipsum dolor sit amet";
        String text2 = "lorem ipsum sequi sit amet";

        LinkedList<diff_match_patch.Diff> diffs = dmp.diff_main(text1, text2);
        dmp.diff_cleanupEfficiency(diffs);
        LinkedList<diff_match_patch.Patch> patches = dmp.patch_make(text1, diffs);
        Message message = new Message(patches, "TestAuthor", 1234);
        message.setDate(new Date(4321));


        String text11 = "lorem dolor sit amet, consectetur adipisicing elit";
        String text22 = "lorem sequi sit amet, consectetur adipisicing elit";
        assertEquals(text22, dmp.patch_apply(patches, text11)[0]);

        String expected = "{\"patches\":[{\"diffs\":[{\"operation\":\"EQUAL\",\"text\":\"sum \"},{\"operation\":\"DELETE\",\"text\":\"dolor\"},{\"operation\":\"INSERT\",\"text\":\"sequi\"},{\"operation\":\"EQUAL\",\"text\":\" sit\"}],\"start1\":8,\"start2\":8,\"length1\":13,\"length2\":13}],\"author\":\"TestAuthor\",\"date\":4321,\"stateId\":1234}";
        assertEquals(expected, Messages.toJson("text", message));
    }
}