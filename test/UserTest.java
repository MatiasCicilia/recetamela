import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

/**
 * Created by Matias Cicilia on 30-Aug-17.
 */
public class UserTest {
    @Test
    public void testSum() {
        int a = 1 + 1;
        assertEquals(2, a);
    }

    @Test
    public void testString() {
        String str = "Hello world";
        assertFalse(str.isEmpty());
    }
}
