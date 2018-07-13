package http.data;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

public class DataDoubleTest {

    @Test
    void setGetTest() {
        DataDouble dataDouble = new DataDouble(2.0);
        dataDouble.setValue(3.0);
        assertThat(dataDouble.getValue()).isEqualTo(3.0);
    }

    @Test
    void equalsSelfTest() {
        DataDouble dataDouble = new DataDouble(5.0);
        assertThat(dataDouble.equals(dataDouble)).isTrue();
    }

    @Test
    void equalsNullTest() {
        DataDouble dataDouble = new DataDouble(5.0);
        assertThat(dataDouble.equals(null)).isFalse();
    }

    @Test
    void equalsStringTest() {
        DataDouble dataDouble = new DataDouble(5.0);
        assertThat(dataDouble.equals("Hoi")).isFalse();
    }

    @Test
    void equalsCorrectTest() {
        DataDouble doubleOne = new DataDouble(5.0);
        DataDouble doubleTwo = new DataDouble(5.0);
        assertThat(doubleOne.equals(doubleTwo)).isTrue();
    }

    @Test
    void equalsFailTest() {
        DataDouble doubleOne = new DataDouble(5.0);
        DataDouble doubleTwo = new DataDouble(3.0);
        assertThat(doubleOne.equals(doubleTwo)).isFalse();
    }
}
