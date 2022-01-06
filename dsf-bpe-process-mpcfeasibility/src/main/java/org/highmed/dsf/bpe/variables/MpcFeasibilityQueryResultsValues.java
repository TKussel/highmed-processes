package org.highmed.dsf.bpe.variables;

import java.util.Map;

import org.camunda.bpm.engine.variable.impl.type.PrimitiveValueTypeImpl;
import org.camunda.bpm.engine.variable.impl.value.PrimitiveTypeValueImpl;
import org.camunda.bpm.engine.variable.type.PrimitiveValueType;
import org.camunda.bpm.engine.variable.value.PrimitiveValue;
import org.camunda.bpm.engine.variable.value.TypedValue;

public class MpcFeasibilityQueryResultsValues
{
	public static interface MpcFeasibilityQueryResultsValue extends PrimitiveValue<MpcFeasibilityQueryResults>
	{
	}

	private static class MpcFeasibilityQueryResultsValueImpl extends PrimitiveTypeValueImpl<MpcFeasibilityQueryResults>
			implements MpcFeasibilityQueryResultsValues.MpcFeasibilityQueryResultsValue
	{
		private static final long serialVersionUID = 1L;

		public MpcFeasibilityQueryResultsValueImpl(MpcFeasibilityQueryResults value, PrimitiveValueType type)
		{
			super(value, type);
		}
	}

	public static class MpcFeasibilityQueryResultsValueTypeImpl extends PrimitiveValueTypeImpl
	{
		private static final long serialVersionUID = 1L;

		private MpcFeasibilityQueryResultsValueTypeImpl()
		{
			super(MpcFeasibilityQueryResults.class);
		}

		@Override
		public TypedValue createValue(Object value, Map<String, Object> valueInfo)
		{
			return new MpcFeasibilityQueryResultsValues.MpcFeasibilityQueryResultsValueImpl((MpcFeasibilityQueryResults) value,
					VALUE_TYPE);
		}
	}

	public static final PrimitiveValueType VALUE_TYPE = new MpcFeasibilityQueryResultsValues.MpcFeasibilityQueryResultsValueTypeImpl();

	private MpcFeasibilityQueryResultsValues()
	{
	}

	public static MpcFeasibilityQueryResultsValues.MpcFeasibilityQueryResultsValue create(MpcFeasibilityQueryResults value)
	{
		return new MpcFeasibilityQueryResultsValues.MpcFeasibilityQueryResultsValueImpl(value, VALUE_TYPE);
	}
}
