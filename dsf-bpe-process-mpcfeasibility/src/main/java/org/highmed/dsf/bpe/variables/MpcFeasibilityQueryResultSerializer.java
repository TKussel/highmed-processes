package org.highmed.dsf.bpe.variables;

import java.io.IOException;
import java.util.Objects;

import org.camunda.bpm.engine.impl.variable.serializer.PrimitiveValueSerializer;
import org.camunda.bpm.engine.impl.variable.serializer.ValueFields;
import org.camunda.bpm.engine.variable.impl.value.UntypedValueImpl;
import org.highmed.dsf.bpe.variables.MpcFeasibilityQueryResultValues.MpcFeasibilityQueryResultValue;
import org.springframework.beans.factory.InitializingBean;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class MpcFeasibilityQueryResultSerializer extends PrimitiveValueSerializer<MpcFeasibilityQueryResultValue>
		implements InitializingBean
{
	private final ObjectMapper objectMapper;

	public MpcFeasibilityQueryResultSerializer(ObjectMapper objectMapper)
	{
		super(MpcFeasibilityQueryResultValues.VALUE_TYPE);

		this.objectMapper = objectMapper;
	}

	@Override
	public void afterPropertiesSet() throws Exception
	{
		Objects.requireNonNull(objectMapper, "objectMapper");
	}

	@Override
	public void writeValue(MpcFeasibilityQueryResultValue value, ValueFields valueFields)
	{
		MpcFeasibilityQueryResult result = value.getValue();
		try
		{
			if (result != null)
				valueFields.setByteArrayValue(objectMapper.writeValueAsBytes(result));
		}
		catch (JsonProcessingException e)
		{
			throw new RuntimeException(e);
		}
	}

	@Override
	public MpcFeasibilityQueryResultValue convertToTypedValue(UntypedValueImpl untypedValue)
	{
		return MpcFeasibilityQueryResultValues.create((MpcFeasibilityQueryResult) untypedValue.getValue());
	}

	@Override
	public MpcFeasibilityQueryResultValue readValue(ValueFields valueFields, boolean asTransientValue)
	{
		byte[] bytes = valueFields.getByteArrayValue();

		try
		{
			MpcFeasibilityQueryResult result = (bytes == null || bytes.length <= 0) ? null
					: objectMapper.readValue(bytes, MpcFeasibilityQueryResult.class);
			return MpcFeasibilityQueryResultValues.create(result);
		}
		catch (IOException e)
		{
			throw new RuntimeException(e);
		}
	}
}
