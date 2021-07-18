package org.devocative.artemis.xml;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamAsAttribute;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@XStreamAlias("assertRs")
public class XAssertRs {
	@XStreamAsAttribute
	private Integer status;
}
