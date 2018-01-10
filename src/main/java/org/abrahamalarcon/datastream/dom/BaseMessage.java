package org.abrahamalarcon.datastream.dom;

import org.apache.commons.lang.StringUtils;

import java.io.Serializable;

public class BaseMessage implements Serializable
{
	protected String getString(String string)
	{
		return StringUtils.isEmpty(string) ? null : string;
	}
}
