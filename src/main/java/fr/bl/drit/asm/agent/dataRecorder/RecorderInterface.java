package fr.bl.drit.asm.agent.dataRecorder;

import org.objectweb.asm.tree.InsnList;

public interface RecorderInterface {

    public InsnList treatMessage(String message);

}
