# java-ASM-flow-agent
A Java agent that captures the control flow and the data flow of an application.
 
# compile agent first
```mvn
mvn clean package
```
We obtain in `target/` the asm-agent.jar agent.
 
```shell
 
java -javaagent:<java-agent-path.jar>=target="<my/package/path>",out="<output/trace/directory>" -jar <targer-java-jar> "<parameters>"
```
 
`<my/package/path>` -> the package path of the app you want to instrument.
 
After the `-jar` parameter please add the java paramter of the `jar` you want to instrument.
 
The trace output is only visible in the terminal.
 
# example
 
see an example of use in project `app_test_for_java_asm_agent` [link](GitHub - Mael-Schiemsky/app_test_for_java_asm_agent).
