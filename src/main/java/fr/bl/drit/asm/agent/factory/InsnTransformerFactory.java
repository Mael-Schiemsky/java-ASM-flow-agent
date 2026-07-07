package fr.bl.drit.asm.agent.factory;

import fr.bl.drit.asm.agent.insnManipulation.InsnTransformer;

public class InsnTransformerFactory {

    public static InsnTransformer getInsnTransformer(String target) {
        InsnTransformer transformer = new InsnTransformer();
        transformer.setTarget(target);
        
        return transformer;
    }
}
