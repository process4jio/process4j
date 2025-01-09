package io.process4j.core;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import io.vertx.core.json.JsonObject;

@JsonPropertyOrder(
{
      "id", "position", "iteration", "durationInMicros", "businessData", "processData", "execution"
})
public final class State
{

   private final String id = UUID.randomUUID().toString();

   private final String context;
   private final Iteration iteration;
   private Flow position;
   private long started;
   private long completed;
   private JsonObject businessData;
   private ProcessData processData;
   private Execution execution;

   public State(final String context, final Flow position, final JsonObject businessData, final ProcessData processData, final Iteration iteration)
   {
      this.context = context;
      this.position = position;
      this.iteration = iteration;
      this.businessData = businessData;
      this.processData = processData == null ? new ProcessData() : processData;
   }

   @JsonIgnore
   public String getId()
   {
      return this.id;
   }

   @JsonProperty("id")
   public String getShortId()
   {
      return this.id;
   }

   @JsonIgnore
   public String getContext()
   {
      return this.context;
   }

   public Flow getPosition()
   {
      return this.position;
   }

   public State setPosition(final Flow position)
   {
      this.position = position;
      return this;
   }

   public JsonObject getBusinessData()
   {
      return this.businessData;
   }

   public ProcessData getProcessData()
   {
      return this.processData;
   }

   public Execution getExecution()
   {
      return this.execution;
   }

   public State setExecution(final Execution execution)
   {
      this.execution = execution;
      this.businessData = execution.getCompletionState().getBusinessData().copy();
      this.processData = execution.getCompletionState().getProcessData().copy();
      return this;
   }

   public Iteration getIteration()
   {
      return this.iteration;
   }

   public long getDurationInMicros()
   {
      return this.getDuration(TimeUnit.MICROSECONDS);
   }

   public long getDuration(final TimeUnit timeUnit)
   {
      return timeUnit.convert(this.completed - this.started, TimeUnit.NANOSECONDS);
   }

   @JsonIgnore
   public long getStarted()
   {
      return this.started;
   }

   public State setStarted(final long started)
   {
      this.started = started;
      return this;
   }

   @JsonIgnore
   public long getCompleted()
   {
      return this.completed;
   }

   public State setCompleted(final long completed)
   {
      this.completed = completed;
      return this;
   }

   @JsonSerialize
   public String position()
   {
      if (this.position == null)
      {
         return null;
      }
      if (this.context == null)
      {
         return this.position.getName();
      }
      return this.context + "." + this.position.getName();
   }
}
