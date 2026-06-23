package fr.bl.drit.asm.agent.methodInjector;

import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.InsnNode;
import org.objectweb.asm.tree.LdcInsnNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.VarInsnNode;
import org.objectweb.asm.Type;

public class MyMethodNode extends MethodNode{

    private String fullyQualifiedName = "";

    public MyMethodNode(int api, int access, String name, String descriptor, String signature, String[] exceptions, String className, MethodVisitor mv) {
        super(api, access, name, descriptor, signature, exceptions);
        this.fullyQualifiedName = className + "#" + name + descriptor + (signature != null ? " " + signature : "");
        this.mv = mv;
    }

    @Override
    public void visitEnd() {
        super.visitEnd();

        if ((access & Opcodes.ACC_ABSTRACT) != 0 || (access & Opcodes.ACC_NATIVE) != 0) {
            accept(mv);
            return;
        }

        onEnter();

        getParameters();

        for (AbstractInsnNode insn : instructions.toArray()) {
            int opcode = insn.getOpcode();
            boolean isReturn = (opcode >= Opcodes.IRETURN && opcode <= Opcodes.RETURN);
            boolean isThrow  = opcode == Opcodes.ATHROW;

            if (!isReturn && !isThrow) continue;

            InsnList exitProbe = new InsnList();

            exitProbe.add(onExit());

            exitProbe.add(getReturnValue(opcode));

            instructions.insertBefore(insn, exitProbe);
        }

        accept(mv);
    }

    private void onEnter() {
        InsnList enterProbe = buildPrintln("e:enter, method:" + fullyQualifiedName);

        if (instructions.size() > 0) {
            instructions.insertBefore(instructions.getFirst(), enterProbe);
        } else {
            instructions.add(enterProbe);
        }
    }

    private void getParameters() {
        InsnList paramProbe = new InsnList();
        Type[] parameterTypes = Type.getArgumentTypes(desc);

        int localIndex = (access & Opcodes.ACC_STATIC) != 0 ? 0 : 1;

        for (Type param : parameterTypes) {
            String message = "p:param, type:" + param + ", value:";

            paramProbe.add(buildPrintln(message));
            
            paramProbe.add(new VarInsnNode(
                    param.getOpcode(Opcodes.ILOAD),
                    localIndex));

            switch (param.getSort()) {
            case Type.BOOLEAN:
                paramProbe.add(new MethodInsnNode(Opcodes.INVOKESTATIC, "java/lang/Boolean", "toString", "(Z)Ljava/lang/String;", false));
                break;
            case Type.BYTE:
                paramProbe.add(new MethodInsnNode(Opcodes.INVOKESTATIC, "java/lang/Byte", "toString", "(B)Ljava/lang/String;", false));
                break;
            case Type.CHAR:
                paramProbe.add(new MethodInsnNode(Opcodes.INVOKESTATIC, "java/lang/Character", "toString", "(C)Ljava/lang/String;", false));
                break;
            case Type.SHORT:
                paramProbe.add(new MethodInsnNode(Opcodes.INVOKESTATIC, "java/lang/Short", "toString", "(S)Ljava/lang/String;", false));
                break;
            case Type.INT:
                paramProbe.add(new MethodInsnNode(Opcodes.INVOKESTATIC, "java/lang/Integer", "toString", "(I)Ljava/lang/String;", false));
                break;
            case Type.FLOAT:
                paramProbe.add(new MethodInsnNode(Opcodes.INVOKESTATIC, "java/lang/Float", "toString", "(F)Ljava/lang/String;", false));
                break;
            case Type.LONG:
                paramProbe.add(new MethodInsnNode(Opcodes.INVOKESTATIC, "java/lang/Long", "toString", "(J)Ljava/lang/String;", false));
                break;
            case Type.DOUBLE:
                paramProbe.add(new MethodInsnNode(Opcodes.INVOKESTATIC, "java/lang/Double", "toString", "(D)Ljava/lang/String;", false));
                break;
            case Type.ARRAY:
            case Type.OBJECT:
            case Type.METHOD:
                paramProbe.add(new MethodInsnNode(Opcodes.INVOKESTATIC, "java/lang/String", "valueOf", "(Ljava/lang/Object;)Ljava/lang/String;", false));
                break;
            case Type.VOID:
                paramProbe.add(buildPrintln("void"));
                break;
            }

            paramProbe.add(buildPrintln(""));

            localIndex += param.getSize();
        }

        instructions.insertBefore(instructions.get(2), paramProbe);
    }

    private InsnList onExit() {
        return buildPrintln("e:exit, method:" + fullyQualifiedName);
    }

    private InsnList getReturnValue(int opcode){
        InsnList returnProbe = new InsnList();

        if (opcode == Opcodes.IRETURN || opcode == Opcodes.FRETURN) {
            returnProbe.add(new InsnNode(Opcodes.DUP));
        } else if (opcode == Opcodes.LRETURN || opcode == Opcodes.DRETURN) {
            returnProbe.add(new InsnNode(Opcodes.DUP2));
        }

        switch (opcode) {
            case Opcodes.IRETURN:
                returnProbe.add(new MethodInsnNode(Opcodes.INVOKESTATIC, "java/lang/Integer", "toString", "(I)Ljava/lang/String;", false));
                break;
            case Opcodes.FRETURN:
                returnProbe.add(new MethodInsnNode(Opcodes.INVOKESTATIC, "java/lang/Float", "toString", "(F)Ljava/lang/String;", false));
                break;
            case Opcodes.LRETURN:
                returnProbe.add(new MethodInsnNode(Opcodes.INVOKESTATIC, "java/lang/Long", "toString", "(J)Ljava/lang/String;", false));
                break;
            case Opcodes.DRETURN:
                returnProbe.add(new MethodInsnNode(Opcodes.INVOKESTATIC, "java/lang/Double", "toString", "(D)Ljava/lang/String;", false));
                break;
            case Opcodes.ARETURN:
            case Opcodes.RETURN:
            case Opcodes.ATHROW:
                returnProbe.add(buildPrintln("void"));
                break;
        }

        if (opcode >= Opcodes.IRETURN && opcode <= Opcodes.DRETURN) {
            returnProbe.add(buildPrintln(""));
        }
        
        return returnProbe;
    }

    private InsnList buildPrintln(String message) {
        InsnList instructions = new InsnList();

        if (!message.isEmpty()) {
            instructions.add(new LdcInsnNode(message));
        }

        instructions.add(new MethodInsnNode(
            Opcodes.INVOKESTATIC,
            "fr/bl/drit/asm/agent/transformer/MyTransformer",
            "jePrint",
            "(Ljava/lang/String;)V",
            false));

        return instructions;
    }
}
