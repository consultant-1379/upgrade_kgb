package com.ericsson.sut.test.operators;

import com.ericsson.cifwk.taf.annotations.Context;
import com.ericsson.cifwk.taf.annotations.Operator;
import com.ericsson.cifwk.taf.tools.cli.CLIOperator;

import java.util.Map;

@Operator(context = Context.CLI)
public class GlassfishCliOperator extends CLIOperator implements GlassfishOperator {

    public Map<String, String> get(String step) {
        return loadData("Glassfish_CliTestData.csv", step);
    }

}

