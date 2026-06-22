package fr.bl.drit.asm.agent.classInjector;

import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

import fr.bl.drit.asm.agent.methodInjector.MyMethodNode;

public class MyClassVisitor extends ClassVisitor {

    private String className = "";

    public MyClassVisitor(ClassVisitor cv) {
        super(Opcodes.ASM9, cv);
    }

    @Override
    public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
        this.className = name;
        super.visit(version, access, name, signature, superName, interfaces);
    }

    @Override
    public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
        MethodVisitor mv = cv.visitMethod(access, name, desc, signature, exceptions);

        return new MyMethodNode(Opcodes.ASM9, access, name, desc, signature, exceptions, className, mv);
    }
}