package fr.bl.drit.asm.agent.insnManipulation;

import java.lang.instrument.ClassFileTransformer;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;

public class InsnTransformer implements ClassFileTransformer{

    private String target = "";

    public void setTarget(String target) {
        this.target = target;
    }

    @Override
    public byte[] transform(ClassLoader loader, String name, Class<?> c, java.security.ProtectionDomain d, byte[] b) {
        if (name.contains(target)) {
            ClassReader reader = new ClassReader(b);
            ClassWriter writer = new ClassWriter(reader, ClassWriter.COMPUTE_FRAMES);

            ClassVisitor visitor = new ClassAnalyzer(writer);

            reader.accept(visitor, 0);

            return writer.toByteArray();
        }
        return null;
    }
}
