package org.lambdaql.analyzer.label;


import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Accessors;
import org.objectweb.asm.Label;

@Getter
@Accessors(fluent = true)
@ToString
@EqualsAndHashCode(of = "label")
public class LabelInfo {
    private final Label label;

    @Setter
    private Object value = null;

    public static LabelInfo of(Label label, Object value) {
        return new LabelInfo(label, value);
    }

    public LabelInfo(Label label, Object value) {
        this.label = label;
        this.value = value;
    }
}
