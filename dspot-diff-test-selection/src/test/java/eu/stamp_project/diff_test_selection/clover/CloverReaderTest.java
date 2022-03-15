package eu.stamp_project.diff_test_selection.clover;

<<<<<<< HEAD
=======
//import static org.junit.jupiter.api.Assertions.*;

>>>>>>> 83a95628a0d0628be0e156673f0269e213b08878
import eu.stamp_project.diff_test_selection.coverage.Coverage;
import org.junit.Test;

import java.util.*;

import static org.junit.Assert.*;

public class CloverReaderTest {

    @Test
    public void read() {
<<<<<<< HEAD
        final String pathToRootOfProject = "src/test/resources/tavern";
        Coverage result = new CloverReader().read(pathToRootOfProject);
=======
//        final String pathToRootOfProject = "src/test/resources/tavern";
//        Coverage result = new CloverReader().read(pathToRootOfProject);
        Coverage result=new CloverReader().read("F:\\tavern");
>>>>>>> 83a95628a0d0628be0e156673f0269e213b08878
    }
}