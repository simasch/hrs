package ch.martinelli.edu.hrs.core.ui;

import com.vaadin.browserless.SpringBrowserlessTest;
import org.junit.jupiter.api.BeforeAll;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Locale;

@SpringBootTest
public abstract class HrsBrowserlessTest extends SpringBrowserlessTest {

    @BeforeAll
    public static void initLocale() {
        Locale.setDefault(Locale.ENGLISH);
    }
}
