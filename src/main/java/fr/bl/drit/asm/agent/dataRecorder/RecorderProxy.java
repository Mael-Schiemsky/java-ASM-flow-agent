package fr.bl.drit.asm.agent.dataRecorder;

import org.objectweb.asm.tree.InsnList;

public class RecorderProxy {

    private static RecorderInterface recorder;

    public static void setRecorder(RecorderInterface recorder) {
        RecorderProxy.recorder = recorder;
    }

    public static InsnList treatMessage(String probeType, String... data) {
        if(recorder != null) {
            return recorder.treatMessage(probeType, data);
        }
        return new InsnList();
    }
}
