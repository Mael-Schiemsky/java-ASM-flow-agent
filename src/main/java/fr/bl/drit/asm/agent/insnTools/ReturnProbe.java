package fr.bl.drit.asm.agent.insnTools;

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.InsnNode;
import org.objectweb.asm.tree.MethodInsnNode;

import static fr.bl.drit.asm.agent.insnTools.PrintTools.buildPrintln;

public class ReturnProbe {

    public static InsnList getReturnValue(int opcode){
        InsnList returnProbe = new InsnList();

        if (opcode == Opcodes.IRETURN || opcode == Opcodes.FRETURN) {
            returnProbe.add(new InsnNode(Opcodes.DUP));
        } else if (opcode == Opcodes.LRETURN || opcode == Opcodes.DRETURN) {
            returnProbe.add(new InsnNode(Opcodes.DUP2));
        }

        returnProbe.add(buildPrintln("[\u001B[35m" + "RETURN" + "\u001B[0m] " + "value:"));

        switch (opcode) {
            case Opcodes.IRETURN ->
                returnProbe.add(new MethodInsnNode(Opcodes.INVOKESTATIC, "java/lang/Integer", "toString", "(I)Ljava/lang/String;", false));
            case Opcodes.FRETURN ->
                returnProbe.add(new MethodInsnNode(Opcodes.INVOKESTATIC, "java/lang/Float", "toString", "(F)Ljava/lang/String;", false));
            case Opcodes.LRETURN ->
                returnProbe.add(new MethodInsnNode(Opcodes.INVOKESTATIC, "java/lang/Long", "toString", "(J)Ljava/lang/String;", false));
            case Opcodes.DRETURN ->
                returnProbe.add(new MethodInsnNode(Opcodes.INVOKESTATIC, "java/lang/Double", "toString", "(D)Ljava/lang/String;", false));
            case Opcodes.ARETURN,
                 Opcodes.RETURN,
                 Opcodes.ATHROW ->
                returnProbe.add(buildPrintln("void"));
        }

        if (opcode >= Opcodes.IRETURN && opcode <= Opcodes.DRETURN) {
            returnProbe.add(buildPrintln(""));
        }
        
        return returnProbe;
    }

}
