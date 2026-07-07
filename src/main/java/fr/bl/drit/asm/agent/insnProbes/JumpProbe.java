package fr.bl.drit.asm.agent.insnProbes;

import static fr.bl.drit.asm.agent.dataRecorder.RecorderProxy.treatMessage;

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.InsnNode;
import org.objectweb.asm.tree.LineNumberNode;
import org.objectweb.asm.tree.MethodInsnNode;

public class JumpProbe {

    public InsnList jumpInsnAnalyse(AbstractInsnNode insn) {
        InsnList insnList = new InsnList();

        boolean isIFsingle = (insn.getOpcode() >= Opcodes.IFEQ && insn.getOpcode() <= Opcodes.IFLE);
        boolean isIFobjSingle = (insn.getOpcode() >= Opcodes.IFNULL && insn.getOpcode() <= Opcodes.IFNONNULL);

        boolean isIFdouble = (insn.getOpcode() >= Opcodes.IF_ICMPEQ && insn.getOpcode() <= Opcodes.IF_ICMPLE);
        boolean isIFobjDouble = (insn.getOpcode() >= Opcodes.IF_ACMPEQ && insn.getOpcode() <= Opcodes.IF_ACMPNE);

        String descriptor = "";

        if(isIFsingle || isIFdouble) {
            descriptor = "(I)Ljava/lang/String;";
        } else {
            descriptor = "(Ljava/lang/Object;)Ljava/lang/String;";
        }

        insnList.add(transformVarToString(isIFsingle || isIFobjSingle, descriptor));

        insnList.add(getCorrespondingLineNumber(insn));

        insnList.add(treatMessage(""));
        if(isIFdouble  || isIFobjDouble){
           insnList.add(treatMessage("")); 
        }

        return insnList;
    }

    private InsnList transformVarToString(boolean single, String descriptor) {
        InsnList toStringInsn = new InsnList();

        if(single) {
            toStringInsn.add(new InsnNode(Opcodes.DUP));
            toStringInsn.add(new MethodInsnNode(Opcodes.INVOKESTATIC, "java/lang/String", "valueOf", descriptor, false));
        } else {
            toStringInsn.add(new InsnNode(Opcodes.DUP2));
            toStringInsn.add(new MethodInsnNode(Opcodes.INVOKESTATIC, "java/lang/String", "valueOf", descriptor, false));
            toStringInsn.add(new InsnNode(Opcodes.SWAP));
            toStringInsn.add(new MethodInsnNode(Opcodes.INVOKESTATIC, "java/lang/String", "valueOf", descriptor, false));
        }

        return toStringInsn;
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

        return treatMessage("[\u001B[33m" + "JUMP" + "\u001B[0m] "  + "instruction corresponding to the line " + myLineNumber);
    }
}
