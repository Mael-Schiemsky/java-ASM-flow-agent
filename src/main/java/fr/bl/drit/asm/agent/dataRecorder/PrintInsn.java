package fr.bl.drit.asm.agent.dataRecorder;

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.LdcInsnNode;
import org.objectweb.asm.tree.MethodInsnNode;

public class PrintInsn implements RecorderInterface {

    public static void printMessage(String message) {
        System.out.println(message);
    }

    public InsnList treatMessage(String probeType, String... data) {
        InsnList instructions = new InsnList();

        if (data.length > 0) {
            String message = "";
            message += colorProbeType(probeType);
            message += String.join(", ", data);
            instructions.add(new LdcInsnNode(message));
        }

        instructions.add(new MethodInsnNode(
            Opcodes.INVOKESTATIC,
            "fr/bl/drit/asm/agent/dataRecorder/PrintInsn",
            "printMessage",
            "(Ljava/lang/String;)V",
            false));

        return instructions;
    }

    private String colorProbeType(String probeType) {
        String coloredProbeType = "";

        switch (probeType) {
            case "ENTER":
                coloredProbeType = "[\u001B[32m" + probeType + "\u001B[0m] ";
                break;
            case "EXIT":
                coloredProbeType = "[\u001B[31m" + probeType + "\u001B[0m] ";
                break;
            case "PARAM":
                coloredProbeType = "[\u001B[36m" + probeType + "\u001B[0m] ";
                break;
            case "JUMP":
                coloredProbeType = "[\u001B[33m" + probeType + "\u001B[0m] ";
                break;
            case "SWITCH":
                coloredProbeType = "[\u001B[34m" + probeType + "\u001B[0m] ";
                break;
            case "RETURN":
                coloredProbeType = "[\u001B[35m" + probeType + "\u001B[0m] ";
                break;
            case "":
                coloredProbeType = "";
                break;
            default:
                coloredProbeType = "[" + probeType + "] ";
        }

        return coloredProbeType;
    }
}
