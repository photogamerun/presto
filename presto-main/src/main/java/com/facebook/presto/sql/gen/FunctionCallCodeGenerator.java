/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.facebook.presto.sql.gen;

import com.facebook.presto.bytecode.BytecodeNode;
import com.facebook.presto.metadata.FunctionRegistry;
import com.facebook.presto.metadata.Signature;
import com.facebook.presto.operator.scalar.ScalarFunctionImplementation;
import com.facebook.presto.spi.type.Type;
import com.facebook.presto.sql.relational.RowExpression;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static com.facebook.presto.operator.scalar.ScalarFunctionImplementation.ArgumentType.VALUE_TYPE;

public class FunctionCallCodeGenerator
        implements BytecodeGenerator
{
    @Override
    public BytecodeNode generateExpression(Signature signature, BytecodeGeneratorContext context, Type returnType, List<RowExpression> arguments)
    {
        FunctionRegistry registry = context.getRegistry();

        ScalarFunctionImplementation function = registry.getScalarFunctionImplementation(signature);

        List<BytecodeNode> argumentsBytecode = new ArrayList<>();
        for (int i = 0; i < arguments.size(); i++) {
            RowExpression argument = arguments.get(i);
            ScalarFunctionImplementation.ArgumentProperty argumentProperty = function.getArgumentProperty(i);
            if (argumentProperty.getArgumentType() == VALUE_TYPE) {
                argumentsBytecode.add(context.generate(argument));
            }
            else {
                argumentsBytecode.add(context.generate(argument, Optional.of(argumentProperty.getLambdaInterface())));
            }
        }

        return context.generateCall(signature.getName(), function, argumentsBytecode);
    }
}
