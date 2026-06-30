package fr.bl.drit.asm.agent.insnTools;

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.LdcInsnNode;
import org.objectweb.asm.tree.MethodInsnNode;

public class PrintTools {

    public static void jePrint(String message) {
        System.out.println(message);
    }

    public static InsnList buildPrintln(String message) {
        InsnList instructions = new InsnList();

        if (!message.isEmpty()) {
            instructions.add(new LdcInsnNode(message));
        }

        instructions.add(new MethodInsnNode(
            Opcodes.INVOKESTATIC,
            "fr/bl/drit/asm/agent/insnTools/PrintTools",
            "jePrint",
            "(Ljava/lang/String;)V",
            false));

        return instructions;
    }

}
