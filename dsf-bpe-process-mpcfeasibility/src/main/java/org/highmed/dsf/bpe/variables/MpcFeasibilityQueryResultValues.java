package org.highmed.dsf.bpe.variables;

import java.util.Map;

import org.camunda.bpm.engine.variable.impl.type.PrimitiveValueTypeImpl;
import org.camunda.bpm.engine.variable.impl.value.PrimitiveTypeValueImpl;
import org.camunda.bpm.engine.variable.type.PrimitiveValueType;
import org.camunda.bpm.engine.variable.value.PrimitiveValue;
import org.camunda.bpm.engine.variable.value.TypedValue;

public class MpcFeasibilityQueryResultValues
{
	public static interface MpcFeasibilityQueryResultValue extends PrimitiveValue<MpcFeasibilityQueryResult>
	{
	}

	private static class MpcFeasibilityQueryResultValueImpl extends PrimitiveTypeValueImpl<MpcFeasibilityQueryResult>
			implements MpcFeasibilityQueryResultValues.MpcFeasibilityQueryResultValue
	{
		private static final long serialVersionUID = 1L;

		public MpcFeasibilityQueryResultValueImpl(MpcFeasibilityQueryResult value, PrimitiveValueType type)
		{
			super(value, type);
		}
	}

	public static class MpcFeasibilityQueryResultValueTypeImpl extends PrimitiveValueTypeImpl
	{
		private static final long serialVersionUID = 1L;

		private MpcFeasibilityQueryResultValueTypeImpl()
		{
			super(MpcFeasibilityQueryResult.class);
		}

		@Override
		public TypedValue createValue(Object value, Map<String, Object> valueInfo)
		{
			return new MpcFeasibilityQueryResultValues.MpcFeasibilityQueryResultValueImpl((MpcFeasibilityQueryResult) value,
					VALUE_TYPE);
		}
	}

	public static final PrimitiveValueType VALUE_TYPE = new MpcFeasibilityQueryResultValues.MpcFeasibilityQueryResultValueTypeImpl();

	private MpcFeasibilityQueryResultValues()
	{
	}

	public static MpcFeasibilityQueryResultValues.MpcFeasibilityQueryResultValue create(MpcFeasibilityQueryResult value)
	{
		return new MpcFeasibilityQueryResultValues.MpcFeasibilityQueryResultValueImpl(value, VALUE_TYPE);
	}
}
