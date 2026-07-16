package fr.bl.drit.asm.agent.insnProbes;

import static fr.bl.drit.asm.agent.dataRecorder.RecorderProxy.treatMessage;
import java.util.ArrayList;
import java.util.Arrays;
import org.objectweb.asm.tree.AbstractInsnNode;

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.InsnNode;
import org.objectweb.asm.tree.MethodInsnNode;

public class ReturnProbe {

    public ArrayList<AbstractInsnNode> getReturnValue(int opcode, AbstractInsnNode insn, ArrayList<AbstractInsnNode> insnList) {
        InsnList returnProbe = new InsnList();

        if (opcode == Opcodes.IRETURN || opcode == Opcodes.FRETURN) {
            returnProbe.add(new InsnNode(Opcodes.DUP));
        } else if (opcode == Opcodes.LRETURN || opcode == Opcodes.DRETURN) {
            returnProbe.add(new InsnNode(Opcodes.DUP2));
        }

        returnProbe.add(treatMessage("RETURN", "value:"));

        returnProbe.add(transformVarToString(opcode));

        if (opcode >= Opcodes.IRETURN && opcode <= Opcodes.DRETURN) {
            returnProbe.add(treatMessage("RETURN"));
        }

        insnList.addAll(insnList.indexOf(insn), Arrays.asList(returnProbe.toArray()));
        return insnList;
    }

    private InsnList transformVarToString(int opcode) {
        InsnList toStringInsn = new InsnList();

        switch (opcode) {
            case Opcodes.IRETURN ->
                toStringInsn.add(new MethodInsnNode(Opcodes.INVOKESTATIC, "java/lang/Integer", "toString", "(I)Ljava/lang/String;", false));
            case Opcodes.FRETURN ->
                toStringInsn.add(new MethodInsnNode(Opcodes.INVOKESTATIC, "java/lang/Float", "toString", "(F)Ljava/lang/String;", false));
            case Opcodes.LRETURN ->
                toStringInsn.add(new MethodInsnNode(Opcodes.INVOKESTATIC, "java/lang/Long", "toString", "(J)Ljava/lang/String;", false));
            case Opcodes.DRETURN ->
                toStringInsn.add(new MethodInsnNode(Opcodes.INVOKESTATIC, "java/lang/Double", "toString", "(D)Ljava/lang/String;", false));
            case Opcodes.ARETURN,
                 Opcodes.RETURN,
                 Opcodes.ATHROW ->
                toStringInsn.add(treatMessage("", "void"));
        }

        return toStringInsn;
    }
}
