package fr.bl.drit.asm.agent.insnProbes;

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.FrameNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.InsnNode;
import org.objectweb.asm.tree.LabelNode;
import org.objectweb.asm.tree.LineNumberNode;
import org.objectweb.asm.tree.MethodInsnNode;

import static fr.bl.drit.asm.agent.dataRecorder.RecorderProxy.treatMessage;

public class SwitchProbe {
    
    public InsnList getSwitchInsnAnalyse(AbstractInsnNode insn) {
        InsnList insnProbes = new InsnList();

        insnProbes.add(getCorrespondingLineNumber(insn));
        insnProbes.add(switchCaseAnalyse(insn));
        insnProbes.add(treatMessage(""));
        
        return insnProbes;
    }

    private InsnList switchCaseAnalyse(AbstractInsnNode insn) {
        InsnList toStringInsn = new InsnList();

        AbstractInsnNode tempInsn = searchPreviousInsn(insn);
        if(isStringHashCodeInsn(tempInsn)) {
            toStringInsn.add(searchPreviousInsn(tempInsn).clone(null));
            return toStringInsn;
        }

        toStringInsn.add(new InsnNode(Opcodes.DUP));
        toStringInsn.add(new MethodInsnNode(Opcodes.INVOKESTATIC, "java/lang/Integer", "toString", "(I)Ljava/lang/String;", false));

        return toStringInsn;
    }

    private AbstractInsnNode searchPreviousInsn(AbstractInsnNode insn) {
        AbstractInsnNode prevInsn = insn.getPrevious();

        while(isNotJBCinsn(prevInsn)) {
            prevInsn = prevInsn.getPrevious();
        }
        return prevInsn;   
    }

    private boolean isStringHashCodeInsn(AbstractInsnNode insn) {
        return insn instanceof MethodInsnNode methodInsn
                && methodInsn.getOpcode() == Opcodes.INVOKEVIRTUAL
                && methodInsn.owner.equals("java/lang/String")
                && methodInsn.name.equals("hashCode")
                && methodInsn.desc.equals("()I");
    }

    private boolean isNotJBCinsn(AbstractInsnNode insn) {
        return insn instanceof LabelNode || insn instanceof LineNumberNode || insn instanceof FrameNode
            || insn.getOpcode() == Opcodes.NOP;
    }

    private InsnList getCorrespondingLineNumber(AbstractInsnNode insn) {
        int myLineNumber = -1;

        AbstractInsnNode prevInsn = insn.getPrevious();
        while (prevInsn != null) {
            if (prevInsn instanceof LineNumberNode lineInsn) {
                myLineNumber = lineInsn.line;
                break;
            }
            prevInsn = prevInsn.getPrevious();
        }

        return treatMessage("[\u001B[33m" + "SWITCH" + "\u001B[0m] " + "switch instruction: " + insn.getOpcode()
                            + " corresponding to the line " + myLineNumber);
    }
}
