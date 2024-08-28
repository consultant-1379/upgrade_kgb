package com.ericsson.sut.test.cases ;

import com.ericsson.cifwk.taf.*;
import com.ericsson.cifwk.taf.annotations.*;
import com.ericsson.cifwk.taf.exceptions.*;
import com.ericsson.cifwk.taf.guice.*;
import com.ericsson.cifwk.taf.tools.cli.*;

import org.testng.annotations.Test;
import javax.inject.Inject;
import java.util.*;

import com.ericsson.sut.test.operators.*;
import com.ericsson.sut.test.getters.*;

public class Glassfish_FunctionalTest extends TorTestCaseHelper implements TestCase {

    @Inject
    private OperatorRegistry<GlassfishOperator> glassfishProvider;

    @Inject
    private GlassfishGetter glassfishGetter;

    @TestId(id = "datadriven", title = "DataDriven Example")
    @Test
    @DataDriven(name = "glassfish_functionaltest")
    public void shouldBePopulatedFromCsv(@Input("first") int x, @Input("second") int y, @Output("result") int expected) {
          assertEquals(x + y, expected);
    }


    /**
     * @DESCRIPTION Check application deployment to glassfish
     * @PRE NONE
     * @PRIORITY HIGH
     */
    @TestId(id = "OSS-23087_Func_1", title = "OSS-23087_Func_1:Check Application Deployment to Glassfish")
    @Context(context = {Context.API,Context.CLI})
    @Test(groups={"Acceptance"})
    public void oSS23087_Func_1CheckApplicationDeploymentToGlassfish() {

        GlassfishOperator glassfishOperator = glassfishProvider.provide(GlassfishOperator.class);
        setTestInfo("TC-1427");
        //TODO VERIFY:STDOUT As Expected
        //TODO VERIFY:EXITCODE As Expected
        throw new TestCaseNotImplementedException();
    }
    /**
     * @DESCRIPTION Check Deployment Time
     * @PRE NONE
     * @PRIORITY HIGH
     */
    @TestId(id = "OSS-23087_Func_2", title = "OSS-23087_Func_2: Check Deployment Time")
    @Context(context = {Context.API,Context.CLI})
    @Test(groups={"Acceptance"})
    public void oSS23087_Func_2CheckDeploymentTime() {

        GlassfishOperator glassfishOperator = glassfishProvider.provide(GlassfishOperator.class);
        setTestInfo("TC-1458");
        //TODO VERIFY:STDOUT As Expected
        //TODO VERIFY:EXITCODE As Expected
        throw new TestCaseNotImplementedException();
    }
}
	
