package org.lambdaql.analyzer.label;


import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.objectweb.asm.Label;

@Getter
@Accessors(fluent = true)
public class LabelInfo {
    private Label label;

    @Setter
    private Object value;

    public static LabelInfo of(Label label, Object value) {
        return new LabelInfo(label, value);
    }

    public LabelInfo(Label label, Object value) {
        this.label = label;
        this.value = value;
    }
}
