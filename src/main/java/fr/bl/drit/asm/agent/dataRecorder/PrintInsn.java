package fr.bl.drit.asm.agent.dataRecorder;

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.LdcInsnNode;
import org.objectweb.asm.tree.MethodInsnNode;

public class PrintInsn implements RecorderInterface {

    public static void printMessage(String message) {
        System.out.println(message);
    }

    public InsnList treatMessage(String message) {
        InsnList instructions = new InsnList();

        if (!message.isEmpty()) {
            instructions.add(new LdcInsnNode(message));
        }

        instructions.add(new MethodInsnNode(
            Opcodes.INVOKESTATIC,
            "fr/bl/drit/asm/agent/dataRecorder/PrintInsn",
            "printMessage",
            "(Ljava/lang/String;)V",
            false));

        return instructions;
    }
}
