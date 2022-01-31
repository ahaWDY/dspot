package eu.stamp_project.diff_test_selection.clover;

//import static org.junit.jupiter.api.Assertions.*;

import eu.stamp_project.diff_test_selection.coverage.Coverage;
import org.junit.Test;

import java.util.*;

import static org.junit.Assert.*;

public class CloverReaderTest {

    @Test
    public void read() {
//        final String pathToRootOfProject = "src/test/resources/tavern";
//        Coverage result = new CloverReader().read(pathToRootOfProject);
        Coverage result=new CloverReader().read("F:\\tavern");
    }
}