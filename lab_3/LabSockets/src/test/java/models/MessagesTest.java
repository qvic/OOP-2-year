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
        String json = "{\"patches\":[{\"diffs\":[{\"operation\":\"EQUAL\",\"text\":\"i \"},{\"operation\":\"DELETE\",\"text\":\"don'\"},{\"operation\":\"INSERT\",\"text\":\"jus\"},{\"operation\":\"EQUAL\",\"text\":\"t kn\"}],\"start1\":0,\"start2\":0,\"length1\":10,\"length2\":9}],\"author\":\"TestAuthor\",\"date\":4321,\"stateId\":1234}";

        LinkedList<diff_match_patch.Diff> diffs = dmp.diff_main("i don't know", "i just know", true);
        dmp.diff_cleanupSemantic(diffs);
        LinkedList<diff_match_patch.Patch> patches = dmp.patch_make("i don't know", diffs);
        Message message = new Message(patches, "TestAuthor", 1234);
        message.setDate(new Date(4321));

        assertEquals(message, Messages.toMessage(json));
    }

    @Test
    public void toJson() {
        LinkedList<diff_match_patch.Diff> diffs = dmp.diff_main("i don't know", "i just know", true);
        dmp.diff_cleanupSemantic(diffs);
        LinkedList<diff_match_patch.Patch> patches = dmp.patch_make("i don't know", diffs);
        Message message = new Message(patches, "TestAuthor", 1234);
        message.setDate(new Date(4321));

        assertEquals("i just know", dmp.patch_apply(patches,"i don't know")[0]);

        String expected = "{\"patches\":[{\"diffs\":[{\"operation\":\"EQUAL\",\"text\":\"i \"},{\"operation\":\"DELETE\",\"text\":\"don'\"},{\"operation\":\"INSERT\",\"text\":\"jus\"},{\"operation\":\"EQUAL\",\"text\":\"t kn\"}],\"start1\":0,\"start2\":0,\"length1\":10,\"length2\":9}],\"author\":\"TestAuthor\",\"date\":4321,\"stateId\":1234}";
        assertEquals(expected, Messages.toJson(message));
    }
}