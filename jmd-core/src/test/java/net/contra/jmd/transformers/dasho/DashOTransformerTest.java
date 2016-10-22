package net.contra.jmd.transformers.dasho;

import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import static org.testng.Assert.*;

public class DashOTransformerTest {

    @Test(dataProvider = "inputData")
    public void testDecrypt(String input, String outputExpected) throws Exception {
        final String outputActual = DashOTransformer.decrypt(input);
        assertEquals(outputActual, outputExpected);
    }


    @DataProvider
    public Object[][] inputData() {
        return new Object[][]{
                {null, null},
                {"<", ";"},
                {"", ""}
                };

    }
}