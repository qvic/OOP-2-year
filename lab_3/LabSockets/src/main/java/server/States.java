package server;

import external.diff_match_patch;
import models.Message;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;

import static external.diff_match_patch.Operation.DELETE;
import static external.diff_match_patch.Operation.EQUAL;
import static external.diff_match_patch.Operation.INSERT;

public class States implements Iterable<Message> {
    private ArrayList<Message> textMessages;
    private diff_match_patch patcher = new diff_match_patch();

    public States() {
        textMessages = new ArrayList<>();
    }

//    public void append(Message message) {
//        if (message.getStateId() == 0 && !textMessages.isEmpty()) {
//            textMessages.clear();
//        }
//
//        if (message.getStateId() == textMessages.size()) {
//            message.setStateId(message.getStateId() + 1);
//            textMessages.add(message);
//            return;
//        }
//
//        String clientStateText = computeState(message.getStateId());
//
//        String clientNewStateText = (String) patcher.patch_apply(
//                message.getPatches(), clientStateText)[0];
//
//        String nextStateText = (String) patcher.patch_apply(
//                getState(message.getStateId() + 1).getPatches(), clientStateText)[0];
//
//        String mergedStateText = diffMerge(nextStateText, clientNewStateText);
//
//        // todo for loop to the end of textMessages
//        // states are 1..size
//        for (int i = message.getStateId() + 2; i <= textMessages.size(); i++) {
//            clientNewStateText = mergedStateText;
//            nextStateText = (String) patcher.patch_apply(
//                    getState(i).getPatches(), nextStateText)[0];
//            mergedStateText = diffMerge(nextStateText, clientNewStateText);
//        }
//
//        LinkedList<diff_match_patch.Patch> patches = patcher.patch_make(nextStateText, mergedStateText);
//        textMessages.add(new Message(patches, message.getAuthor(), textMessages.size() + 1));
//    }

//    public Message getLast() {
//        if (!textMessages.isEmpty()) {
//            return textMessages.get(textMessages.size() - 1);
//        }
//        return null;
//    }

    String diffMerge(String text1, String text2) {
        return diffMerge(text1, text2, patcher);
    }

    public static String diffMerge(String text1, String text2, diff_match_patch dmp) {
        LinkedList<diff_match_patch.Diff> diffs = dmp.diff_main(text1, text2);
        dmp.diff_cleanupSemantic(diffs);
        StringBuilder merged = new StringBuilder();
        for (diff_match_patch.Diff diff : diffs) {
            merged.append(diff.text);
        }
        return merged.toString();
    }

//    String computeState(int stateId) {
//        String result = "";
//        for (int i = 1; i <= stateId; i++) {
//            result = (String) patcher.patch_apply(getState(i).getPatches(), result)[0];
//        }
//        return result;
//    }
//
//    Message getState(int stateId) {
//        return textMessages.get(stateId - 1);
//    }
//
    @Override
    public Iterator<Message> iterator() {
        return textMessages.iterator();
    }
}
