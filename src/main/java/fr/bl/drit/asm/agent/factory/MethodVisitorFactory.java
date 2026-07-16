package fr.bl.drit.asm.agent.factory;

import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;

import fr.bl.drit.asm.agent.insnManipulation.MethodProbesInjector;
import fr.bl.drit.asm.agent.insnProbes.BanProb;
import fr.bl.drit.asm.agent.insnProbes.JumpProbe;
import fr.bl.drit.asm.agent.insnProbes.ParametersProbe;
import fr.bl.drit.asm.agent.insnProbes.ReturnProbe;
import fr.bl.drit.asm.agent.insnProbes.SwitchProbe;

public class MethodVisitorFactory {

    public static MethodVisitor getMethodProbesInjector(
        String className, int api, int access, String name,
        String descriptor, String signature, String[] exceptions, ClassVisitor cv) {
            
        MethodVisitor mv = cv.visitMethod(access, name, descriptor, signature, exceptions);
        MethodProbesInjector methodInjector = new MethodProbesInjector(api, access, name, descriptor, signature, exceptions, className, mv);

        BanProb ban = new BanProb();
        methodInjector.setParametersProbe(new ParametersProbe());
        methodInjector.setJumpProbe(new JumpProbe());
        methodInjector.setSwitchProbe(new SwitchProbe(ban));
        methodInjector.setBanProb(ban);
        methodInjector.setReturnProbe(new ReturnProbe());
        
        return methodInjector;
    }
}
