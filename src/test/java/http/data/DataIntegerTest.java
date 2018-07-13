package http.data;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

public class DataIntegerTest {

    @Test
    void setGetTest() {
        DataInteger dataInteger = new DataInteger(2);
        dataInteger.setValue(3);
        assertThat(dataInteger.getValue()).isEqualTo(3);
    }

    @Test
    void equalsSelfTest() {
        DataInteger dataInteger = new DataInteger(5);
        assertThat(dataInteger.equals(dataInteger)).isTrue();
    }

    @Test
    void equalsNullTest() {
        DataInteger dataInteger = new DataInteger(5);
        assertThat(dataInteger.equals(null)).isFalse();
    }

    @Test
    void equalsStringTest() {
        DataInteger dataInteger = new DataInteger(5);
        assertThat(dataInteger.equals("Hoi")).isFalse();
    }

    @Test
    void equalsCorrectTest() {
        DataInteger intOne = new DataInteger(5);
        DataInteger intTwo = new DataInteger(5);
        assertThat(intOne.equals(intTwo)).isTrue();
    }

    @Test
    void equalsFailTest() {
        DataInteger intOne = new DataInteger(5);
        DataInteger intTwo = new DataInteger(3);
        assertThat(intOne.equals(intTwo)).isFalse();
    }
}

