package fr.bl.drit.asm.agent.insnProbes;

import static fr.bl.drit.asm.agent.dataRecorder.RecorderProxy.treatMessage;

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.VarInsnNode;
import org.objectweb.asm.Type;

public class ParametersProbe {

    public InsnList getParameters(int access, String desc, InsnList instructions) {
        InsnList paramProbe = new InsnList();
        Type[] parameterTypes = Type.getArgumentTypes(desc);

        int localIndex = (access & Opcodes.ACC_STATIC) != 0 ? 0 : 1;

        for (Type param : parameterTypes) {
            String message = "[\u001B[36m" + "PARAM" + "\u001B[0m] " + "type: " + param + ", value:";

            paramProbe.add(treatMessage(message));

            paramProbe.add(new VarInsnNode(
                    param.getOpcode(Opcodes.ILOAD),
                    localIndex));

            paramProbe.add(transformVarToString(param.getSort()));

            paramProbe.add(treatMessage(""));

            localIndex += param.getSize();
        }

        return paramProbe;
    }

    private InsnList transformVarToString(int sort) {
        InsnList toStringInsn = new InsnList();

        switch (sort) {
            case Type.BOOLEAN ->
                toStringInsn.add(new MethodInsnNode(Opcodes.INVOKESTATIC, "java/lang/Boolean", "toString", "(Z)Ljava/lang/String;", false));
            case Type.BYTE ->
                toStringInsn.add(new MethodInsnNode(Opcodes.INVOKESTATIC, "java/lang/Byte", "toString", "(B)Ljava/lang/String;", false));
            case Type.CHAR ->
                toStringInsn.add(new MethodInsnNode(Opcodes.INVOKESTATIC, "java/lang/Character", "toString", "(C)Ljava/lang/String;", false));
            case Type.SHORT ->
                toStringInsn.add(new MethodInsnNode(Opcodes.INVOKESTATIC, "java/lang/Short", "toString", "(S)Ljava/lang/String;", false));
            case Type.INT ->
                toStringInsn.add(new MethodInsnNode(Opcodes.INVOKESTATIC, "java/lang/Integer", "toString", "(I)Ljava/lang/String;", false));
            case Type.FLOAT ->
                toStringInsn.add(new MethodInsnNode(Opcodes.INVOKESTATIC, "java/lang/Float", "toString", "(F)Ljava/lang/String;", false));
            case Type.LONG ->
                toStringInsn.add(new MethodInsnNode(Opcodes.INVOKESTATIC, "java/lang/Long", "toString", "(J)Ljava/lang/String;", false));
            case Type.DOUBLE ->
                toStringInsn.add(new MethodInsnNode(Opcodes.INVOKESTATIC, "java/lang/Double", "toString", "(D)Ljava/lang/String;", false));
            case Type.ARRAY,
                 Type.OBJECT,
                 Type.METHOD ->
                toStringInsn.add(new MethodInsnNode(Opcodes.INVOKESTATIC, "java/lang/String", "valueOf", "(Ljava/lang/Object;)Ljava/lang/String;", false));
            }

        return toStringInsn;
    }
}
