package fr.bl.drit.asm.agent.insnProbes;

import static fr.bl.drit.asm.agent.dataRecorder.RecorderProxy.treatMessage;

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.InsnNode;
import org.objectweb.asm.tree.MethodInsnNode;

public class ReturnProbe {

    public InsnList getReturnValue(int opcode){
        InsnList returnProbe = new InsnList();

        if (opcode == Opcodes.IRETURN || opcode == Opcodes.FRETURN) {
            returnProbe.add(new InsnNode(Opcodes.DUP));
        } else if (opcode == Opcodes.LRETURN || opcode == Opcodes.DRETURN) {
            returnProbe.add(new InsnNode(Opcodes.DUP2));
        }

        returnProbe.add(treatMessage("[\u001B[35m" + "RETURN" + "\u001B[0m] " + "value:"));

        returnProbe.add(transformVarToString(opcode));

        if (opcode >= Opcodes.IRETURN && opcode <= Opcodes.DRETURN) {
            returnProbe.add(treatMessage(""));
        }
        
        return returnProbe;
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
                toStringInsn.add(treatMessage("void"));
        }

        return toStringInsn;
    }
}
