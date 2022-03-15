package eu.stamp_project.dspot.selector.branchcoverageselector.clover;

import eu.stamp_project.dspot.selector.branchcoverageselector.Coverage;
import org.junit.Test;

<<<<<<< HEAD
import static org.junit.Assert.*;

public class CloverReaderTest {

    @Test
    public void read() {
        Coverage result=new CloverReader().read("F:\\jsoup");
=======
import static org.junit.jupiter.api.Assertions.*;

public class CloverReaderTest {
    @Test
    public void read() {
        Coverage result=new CloverReader().read("F:\\tavern");
>>>>>>> 83a95628a0d0628be0e156673f0269e213b08878
    }
}