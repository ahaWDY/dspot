package eu.stamp_project.prettifier;

import eu.stamp_project.prettifier.options.UserInput;
import spoon.reflect.declaration.CtMethod;

import java.util.List;

/***
 * Created by Carolin Brandt
 * c.e.brandt@tudelft.nl
 * on 27/10/21
 */
public interface Prettifier {

    /**
     * prettify the given list of test cases and return the prettified ones
     */
    List<CtMethod<?>> prettify(List<CtMethod<?>> amplifiedTestsToBePrettified);


}
