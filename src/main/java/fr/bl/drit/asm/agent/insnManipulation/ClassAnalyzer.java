package fr.bl.drit.asm.agent.insnManipulation;

import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

import static fr.bl.drit.asm.agent.factory.MethodVisitorFactory.getMethodProbesInjector;

public class ClassAnalyzer extends ClassVisitor {

    private String className = "";
    private static int apiVersion = Opcodes.ASM9;

    public ClassAnalyzer(ClassVisitor cv) {
        super(apiVersion, cv);
    }

    @Override
    public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
        this.className = name;
        super.visit(version, access, name, signature, superName, interfaces);
    }

    @Override
    public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
        return getMethodProbesInjector(className, apiVersion, access, name, desc, signature, exceptions, cv);
    }
}