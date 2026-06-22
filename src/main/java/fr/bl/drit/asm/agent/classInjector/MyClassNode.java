package fr.bl.drit.asm.agent.classInjector;

import org.objectweb.asm.tree.ClassNode;

import org.objectweb.asm.ClassVisitor;

import org.objectweb.asm.Opcodes;

public class MyClassNode extends ClassNode {

    public MyClassNode() {
        super(Opcodes.ASM9);
    }

    @Override
    public void accept(ClassVisitor cv) {
        super.accept(new MyClassVisitor(cv));
    }
}
