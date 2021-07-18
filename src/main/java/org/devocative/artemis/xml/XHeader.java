package org.devocative.artemis.xml;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamAsAttribute;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@XStreamAlias("header")
public class XHeader {
	@XStreamAsAttribute
	private String name;

	@XStreamAsAttribute
	private String value;
}
