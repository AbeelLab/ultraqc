package tsv;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

public class QcTest {

    private Qc qc;
    private Tsv tsv;

    @BeforeEach
    public void setup() {
        Map<String, Tsv> map = new HashMap<>();
        tsv = mock(Tsv.class);
        map.put("testQC", tsv);
        qc = new Qc(map);
    }

    @Test
    public void getTsvMap() {
        Tsv tsv = qc.getTsvMap().get("testQC");
        assertThat(qc.getTsvMap().get("testQC")).isEqualTo(tsv);
    }
}
