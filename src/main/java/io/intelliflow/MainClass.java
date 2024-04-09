package io.intelliflow;

import io.intelliflow.tester.RunTest;
import io.quarkus.runtime.Quarkus;

public class MainClass {

	public static void main(String... args) {
        Quarkus.run(RunTest.class, args);
    }

}
