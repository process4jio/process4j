package io.process4j.core;

import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonIgnore;

import io.process4j.core.infix.BooleanEvaluator;
import io.process4j.core.infix.Variables;
import io.vertx.core.json.JsonObject;
import io.vertx.core.json.pointer.JsonPointer;

public final class Rule
{
   public static final String VARIABLE_REGEX = P4J.getString(P4J.PROPERTIES_RULE_VARIABLE_REGEX);

   private static final String REPLACEMENT_REGEX = "^\"|\"$";

   @JsonIgnore
   private final Pointers expressionPointers;

   @JsonIgnore
   private final Pointers resultPointers = null;

   @JsonIgnore
   private final Variables variables;

   private final String expression;
   private final String result;

   public Rule(final String expression, final String result)
   {
      this.expression = expression;
      this.result = result;

      this.expressionPointers = new Pointers(expression);
      // this.resultPointers = new Pointers(result);

      final int size = (int) Math.ceil(this.expressionPointers.size() / 0.75);
      this.variables = new Variables(size);
   }

   boolean match(final BooleanEvaluator evaluator, final JsonObject businessData, final ProcessData processData)
   {
      this.variables.clear();

      this.expressionPointers.getProcessDataPointers().forEach(ptr -> {
         final Object obj = ptr.queryJson(processData.getData());
         this.variables.set(ptr.toString(), String.valueOf(obj).replaceAll(REPLACEMENT_REGEX, "")); // null => 'null'
      });

      this.expressionPointers.getBusinessDataPointers().forEach(ptr -> {
         final Object obj = ptr.queryJson(businessData);
         this.variables.set(ptr.toString(), String.valueOf(obj).replaceAll(REPLACEMENT_REGEX, "")); // null => 'null'
      });

      return Boolean.parseBoolean(evaluator.evaluate(this.expression, this.variables));
   }

   String result(final JsonObject businessData, final ProcessData processData)
   {
      return this.result; // TODO: return evaluated result if result has evaluate markers around pointer
   }

   // TODO: Notation in rule to trigger
   String evaluate(final JsonObject businessData, final ProcessData processData)
   {
      String evaluatedResult = this.result;
      for (final JsonPointer ptr : this.resultPointers.getProcessDataPointers())
      {
         final String key = ptr.toString();
         final String value = String.valueOf(ptr.queryJson(processData.getData())).replaceAll(REPLACEMENT_REGEX, "");
         evaluatedResult = evaluatedResult.replaceAll(key, value);
      }
      for (final JsonPointer ptr : this.resultPointers.getBusinessDataPointers())
      {
         final String key = ptr.toString();
         final String value = String.valueOf(ptr.queryJson(businessData)).replaceAll(REPLACEMENT_REGEX, "");
         evaluatedResult = evaluatedResult.replaceAll(key, value);
      }
      return evaluatedResult;
   }

   public String getExpression()
   {
      return this.expression;
   }

   public String getResult()
   {
      return this.result;
   }

   @Override
   public int hashCode()
   {
      return Objects.hash(this.expression, this.result);
   }

   @Override
   public boolean equals(final Object obj)
   {
      if (this == obj)
      {
         return true;
      }
      if (obj == null || this.getClass() != obj.getClass())
      {
         return false;
      }
      final Rule other = (Rule) obj;
      if (!Objects.equals(this.expression, other.expression) || !Objects.equals(this.result, other.result))
      {
         return false;
      }
      return true;
   }
}
