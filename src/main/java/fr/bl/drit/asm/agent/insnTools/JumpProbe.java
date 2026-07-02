package fr.bl.drit.asm.agent.insnTools;

import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.LineNumberNode;

import static fr.bl.drit.asm.agent.insnTools.PrintTools.buildPrintln;

public class JumpProbe {

    public static InsnList jumpInsnAnalyse(AbstractInsnNode insn) {
        InsnList insnList = new InsnList();
        
        insnList.add(getCorrespondingLineNumber(insn));

        //TODO : calcul val des arguments du jump

        return insnList;
    }

    private static InsnList getCorrespondingLineNumber(AbstractInsnNode insn) {
        int myLineNumber = -1;

        AbstractInsnNode prevInsn = insn.getPrevious();
        while (prevInsn != null) {
            if (prevInsn instanceof LineNumberNode lineInsn) {
                myLineNumber = lineInsn.line;
                break;
            }
            prevInsn = prevInsn.getPrevious();
        }

        return buildPrintln("[\u001B[33m" + "JUMP" + "\u001B[0m] "  + "instruction corresponding to the line " + myLineNumber);
    }
}
