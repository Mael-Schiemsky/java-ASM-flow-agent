package fr.bl.drit.asm.agent.agentMains;

import java.io.PrintStream;
import java.lang.instrument.Instrumentation;
import java.util.HashMap;
import java.util.Map;

import fr.bl.drit.asm.agent.dataRecorder.PrintInsn;
import fr.bl.drit.asm.agent.dataRecorder.RecorderInterface;
import fr.bl.drit.asm.agent.dataRecorder.RecorderProxy;
import fr.bl.drit.asm.agent.insnManipulation.InsnTransformer;
import static fr.bl.drit.asm.agent.factory.InsnTransformerFactory.getInsnTransformer;

public class AsmAgentMain {

    public static void premain(String agentArgs, Instrumentation inst) {
        init(agentArgs, inst);
        System.out.println("Agent loaded at startup");
    }

    public static void agentmain(String agentArgs, Instrumentation inst) {
        init(agentArgs, inst);
        System.out.println("Agent attached dynamically");        
    }

    private static void init(String agentArgs, Instrumentation inst) {
        
        final Map<String, String> args = parseArgs(agentArgs);
        final String target = args.get("target");
        final String outputPath = args.get("out");

        if (target.isEmpty()) {
            System.err.println(
                "[flow-agent] No 'target' provided in agent arguments; instrumenter will be disabled.");
            printUsage(System.err);
            return;
        }

        if (outputPath == null) {
            System.err.println(
                "[flow-agent] No 'out' provided in agent arguments; instrumenter will be disabled.");
            printUsage(System.err);
            return;
        }

        initRecorderProxy();
        addTransformer(inst, target, outputPath);
    }

    private static void initRecorderProxy() {
        RecorderInterface recorder = new PrintInsn();
        RecorderProxy.setRecorder(recorder);
    }

    private static void addTransformer(Instrumentation inst, String target, String outputPath) {
        InsnTransformer transformer = getInsnTransformer(target);
        
        inst.addTransformer(transformer, true);
    }

    private static Map<String, String> parseArgs(String args) {
        Map<String, String> map = new HashMap<>();

        if (args == null || args.trim().isEmpty()) {
            return map;
        }

        String[] pairs = args.split(",");
        for (String pair : pairs) {
            String trimmed = pair.trim();
            if (trimmed.isEmpty()) continue;

            String[] kv = trimmed.split("=", 2);
            String key = kv[0].trim();
            String value = kv[1].trim();

            if (key.isEmpty() || value.isEmpty()) {
                System.err.println("[flow-agent] Ignoring empty key/value in agent argument: " + trimmed);
                continue;
            }
            map.put(key, value);
        }
        return map;
    }

    private static void printUsage(PrintStream out) {
        out.println(
            "[ASM-agent] Usage: target=<prefix[+prefix...]>,out=<dir>");
        out.println("  target   : '+'-separated list of class name prefixes to instrument (required)");
        out.println("  out      : output directory for flow files and method ID mapping (required)");
    }
}
