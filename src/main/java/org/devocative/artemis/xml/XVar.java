package org.devocative.artemis.xml;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamAsAttribute;
import com.thoughtworks.xstream.annotations.XStreamConverter;
import com.thoughtworks.xstream.converters.extended.ToAttributedValueConverter;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@XStreamAlias("var")
@XStreamConverter(value = ToAttributedValueConverter.class, strings = "valueAsBody")
public class XVar implements INameTheValue {
	@XStreamAsAttribute
	private String name;

	@XStreamAlias("value")
	@XStreamAsAttribute
	private String valueAsAttr;

	private String valueAsBody;

	// ------------------------------

	@Override
	public String getValue() {
		return !getValueAsBody().isEmpty() ? getValueAsBody() : getValueAsAttr();
	}

	@Override
	public String toString() {
		return String.format("Var: '%s' = '%s'", getName(), getValue());
	}
}
