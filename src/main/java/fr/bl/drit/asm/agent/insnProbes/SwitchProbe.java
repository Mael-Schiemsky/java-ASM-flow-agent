package fr.bl.drit.asm.agent.insnProbes;

import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.InsnList;

public class SwitchProbe {
    
    public InsnList getSwitchInsnAnalyse(AbstractInsnNode insn) {
        InsnList insnProbes = new InsnList();
        return insnProbes;
    }
}
