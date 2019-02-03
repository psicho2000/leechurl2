package de.psicho.LeechUrl;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;

import de.psicho.LeechUrl.Prediction.Slice;

public class PredictionTest {

    @Test
    public void noArgsShouldProvideDefaultValues() {
        String[] args = new String[0];

        Slice slice = new Slice(args);

        assertThat(slice.from).isEqualTo(0);
        assertThat(slice.to).isEqualTo(Integer.MAX_VALUE);
    }

    @Test
    public void onArgShouldProvideConvertedArgAndDefaultValue() {
        String[] args = new String[1];
        args[0] = "5";

        Slice slice = new Slice(args);

        assertThat(slice.from).isEqualTo(5);
        assertThat(slice.to).isEqualTo(Integer.MAX_VALUE);
    }

    @Test
    public void twoArgsShouldProvideConvertedArgs() {
        String[] args = new String[2];
        args[0] = "5";
        args[1] = "10";

        Slice slice = new Slice(args);

        assertThat(slice.from).isEqualTo(5);
        assertThat(slice.to).isEqualTo(10);
    }

    @Test
    public void noIntArgsShouldProvideDefaultValues() {
        String[] args = new String[2];
        args[0] = "five";
        args[1] = "ten";

        Slice slice = new Slice(args);

        assertThat(slice.from).isEqualTo(0);
        assertThat(slice.to).isEqualTo(Integer.MAX_VALUE);
    }
}
