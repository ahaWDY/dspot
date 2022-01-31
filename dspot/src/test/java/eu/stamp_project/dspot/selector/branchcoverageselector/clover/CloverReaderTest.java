package eu.stamp_project.dspot.selector.branchcoverageselector.clover;

import eu.stamp_project.dspot.selector.branchcoverageselector.Coverage;
import org.junit.Test;

import static org.junit.jupiter.api.Assertions.*;

public class CloverReaderTest {
    @Test
    public void read() {
        Coverage result=new CloverReader().read("F:\\tavern");
    }
}