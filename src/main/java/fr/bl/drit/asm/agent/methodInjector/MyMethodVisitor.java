package fr.bl.drit.asm.agent.methodInjector;

import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

public class MyMethodVisitor extends MethodVisitor{

    private final String className;
    private final String name;
    private final String desc;

    public MyMethodVisitor(int api, MethodVisitor methodVisitor, String className, String name, String desc) {
        super(api, methodVisitor);
        this.className = className;
        this.name = name;
        this.desc = desc;
    }

    @Override
    public void visitCode() {
        injectPrintln("e:enter, method:" + className + "#" + name + desc);
        super.visitCode();
    }
    
    @Override
    public void visitInsn(int opcode) {
        if(opcode >= Opcodes.IRETURN && opcode <= Opcodes.RETURN || opcode == Opcodes.ATHROW) {
            injectPrintln("e:exit, method:" + className + "#" + name + desc);
        }

        if(opcode == Opcodes.IRETURN || opcode == Opcodes.FRETURN){
            mv.visitInsn(Opcodes.DUP);
        } else if(opcode == Opcodes.LRETURN || opcode == Opcodes.DRETURN){
            mv.visitInsn(Opcodes.DUP2);
        }

        switch (opcode) {
            case Opcodes.IRETURN:
                mv.visitMethodInsn(Opcodes.INVOKESTATIC, "java/lang/Integer", "toString", "(I)Ljava/lang/String;", false);
                break;
            case Opcodes.FRETURN:
                mv.visitMethodInsn(Opcodes.INVOKESTATIC, "java/lang/Float", "toString", "(F)Ljava/lang/String;", false);
                break;
            case Opcodes.LRETURN:
                mv.visitMethodInsn(Opcodes.INVOKESTATIC, "java/lang/Long", "toString", "(J)Ljava/lang/String;", false);
                break;
            case Opcodes.DRETURN:
                mv.visitMethodInsn(Opcodes.INVOKESTATIC, "java/lang/Double", "toString", "(D)Ljava/lang/String;", false);
                break;
            case Opcodes.ARETURN:
            case Opcodes.RETURN:
            case Opcodes.ATHROW:
                injectPrintln("void");
                break;
        }

        if(opcode >= Opcodes.IRETURN && opcode <= Opcodes.DRETURN) {
            injectPrintln("");
        }

        super.visitInsn(opcode);
    }

    public void injectPrintln(String message) {
        mv.visitFieldInsn(
            Opcodes.GETSTATIC,
            "java/lang/System",
            "out",
            "Ljava/io/PrintStream;"
        );

        if(message != "") {
            mv.visitLdcInsn(message);
        }
        else{
            mv.visitInsn(Opcodes.SWAP);
        }

        mv.visitMethodInsn(
            Opcodes.INVOKEVIRTUAL,
            "java/io/PrintStream",
            "println",
            "(Ljava/lang/String;)V",
            false
        );
    }
}