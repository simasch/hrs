package ch.martinelli.edu.hrs.core.ui;

import ch.martinelli.edu.hrs.TestcontainersConfiguration;
import com.vaadin.browserless.SpringBrowserlessTest;
import org.junit.jupiter.api.BeforeAll;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;

import java.util.Locale;

@Import(TestcontainersConfiguration.class)
@SpringBootTest
public abstract class HrsBrowserlessTest extends SpringBrowserlessTest {

    @BeforeAll
    public static void initLocale() {
        Locale.setDefault(Locale.ENGLISH);
    }
}
