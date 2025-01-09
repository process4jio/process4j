package io.process4j.core;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import io.process4j.core.annotations.Foreach;
import io.process4j.core.annotations.While;
import io.process4j.core.infix.BooleanEvaluator;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.core.json.pointer.JsonPointer;

@JsonPropertyOrder(
{
      "process", "started", "completion", "position", "errorType", "errorMessage", "durationInMillis", "durationInMicros", "states"
})
public final class Execution
{

   private static final Logger LOG = Logger.getLogger(Execution.class.getName());

   private static final String MAX_ITERATIONS_EXCEEDED = "Number of iterations exceeds the maximum limit of %s";
   private static final String PARALLELISM_UNSUPPORTED = "Parallel iterations not (yet) supported. Please revise activity %s";
   private static final String NOTHING_TO_ITERATE = "No items to iterate, Node %s is not executed";
   private static final String NO_LOOP = "Do while condition initially false. Node %s is not executed";

   private static final int MAX_ITERATIONS = P4J.getInteger(P4J.PROPERTIES_MAX_ITERATIONS);

   private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

   private final String processName;

   private String startTime;

   private long started;

   private Iteration iteration;

   private long completed;

   private Completion completion;

   private Exception exception;

   private final BooleanEvaluator evaluator = new BooleanEvaluator();

   private final List<State> states = new ArrayList<>();

   public Execution(final String processName)
   {
      this.processName = processName;
   }

   @JsonProperty("process")
   public String getProcessName()
   {
      return this.processName;
   }

   @JsonProperty("started")
   public String getStartTime()
   {
      return this.startTime;
   }

   public List<State> getStates()
   {
      return this.states;
   }

   public void setCompleted(final long completed)
   {
      this.completed = completed;
   }

   public Completion getCompletion()
   {
      return this.completion;
   }

   public void setCompletion(final Completion completion)
   {
      this.completion = completion;
   }

   @JsonIgnore
   public State getCompletionState()
   {
      return this.completion == null ? null : this.getCurrentState();
   }

   public String getErrorType()
   {
      return this.exception == null ? null : this.exception.getClass().getName();
   }

   public String getErrorMessage()
   {
      return this.exception == null ? null : this.exception.getMessage();
   }

   @JsonIgnore
   public Exception getException()
   {
      return this.exception;
   }

   public void setException(final Exception exception)
   {
      this.exception = exception;
   }

   @JsonIgnore
   public State getCurrentState()
   {
      return this.states.get(this.states.size() - 1);
   }

   @JsonIgnore
   public String getCurrentContext()
   {
      return this.getCurrentState().getContext();
   }

   @JsonIgnore
   public Flow getCurrentPosition()
   {
      return this.getCurrentState().getPosition();
   }

   @JsonProperty("position")
   public String getCurrentPositionName()
   {
      return this.getCurrentPosition().getName();
   }

   private State getNextState(final Iteration iteration)
   {
      return new State(this.getCurrentContext(), this.getCurrentPosition(), this.copyCurrentBusinessData(), this.copyCurrentProcessData(), iteration);
   }

   private JsonObject currentBusinessData()
   {
      return this.states.isEmpty() ? null : this.states.get(this.states.size() - 1).getBusinessData();
   }

   private ProcessData currentProcessData()
   {
      return this.states.isEmpty() ? null : this.states.get(this.states.size() - 1).getProcessData();
   }

   JsonObject copyCurrentBusinessData()
   {
      return this.states.isEmpty() || this.getCurrentState().getBusinessData() == null ? null : this.getCurrentState().getBusinessData().copy();
   }

   ProcessData copyCurrentProcessData()
   {
      return this.states.isEmpty() || this.getCurrentState().getProcessData() == null ? null : this.getCurrentState().getProcessData().copy();
   }

   void start(final State state) throws ExecutionException
   {
      this.startTime = LocalDateTime.now().format(formatter);
      this.started = System.nanoTime();
      this.states.add(state);
      this.iteration = state.getIteration();
      this.advance();
   }

   /**
    * Advances current execution of this process instance until ending normally or accidentally in a failure
    *
    * @throws ExecutionException
    * @throws InitializationException
    *
    * @throws Exception
    *
    */
   private void advance() throws ExecutionException
   {
      // Get node to be executed
      final BaseNode<?> node = this.getCurrentPosition().getTarget();

      // Complete normally
      if (node == null)
      {
         return;
      }

      Object loopAnnotation;

      if (node instanceof BaseActivityNode && (loopAnnotation = node.getClass().getDeclaredAnnotation(While.class)) != null)
      {
         final While whileLoop = (While) loopAnnotation;
         final Rule rule = new Rule(whileLoop.expression(), Boolean.toString(true));

         // Skip node execution
         if (!this.condition(rule, this.currentBusinessData(), this.currentProcessData()))
         {
            this.noOp((BaseActivityNode<?>) node, String.format(NO_LOOP, node.getName()));
         }

         // Loop
         else
         {
            Iteration nodeIteration = Iteration.nr(0, this.iteration);

            while (this.condition(rule, this.currentBusinessData(), this.currentProcessData()))
            {
               if (nodeIteration.value() == MAX_ITERATIONS)
               {
                  throw new ExecutionException(String.format(MAX_ITERATIONS_EXCEEDED, MAX_ITERATIONS));
               }
               this.executeNode(node, nodeIteration);
               if (nodeIteration.isTerminated())
               {
                  break;
               }
               nodeIteration = Iteration.nr(nodeIteration.value() + 1, this.iteration);
            }
         }
      }

      else if (node instanceof BaseActivityNode && (loopAnnotation = node.getClass().getDeclaredAnnotation(Foreach.class)) != null)
      {
         final Foreach foreachLoop = (Foreach) loopAnnotation;

         int nrOfItems;
         final Object o = JsonPointer.from(foreachLoop.expression()).queryJsonOrDefault(foreachLoop.expression().startsWith("//") ? this.currentProcessData().getData() : this.currentBusinessData(),
               new JsonArray());
         if (o instanceof JsonObject)
         {
            nrOfItems = JsonObject.mapFrom(o).size();
         }
         else
         {
            nrOfItems = ((JsonArray) o).size();
         }

         if (foreachLoop.parallel())
         {
            LOG.warning(String.format(PARALLELISM_UNSUPPORTED, node.getName()));
         }

         // Skip node execution
         if (nrOfItems == 0)
         {
            this.noOp((BaseActivityNode<?>) node, String.format(NOTHING_TO_ITERATE, node.getName()));
         }

         // Foreach
         else
         {
            // Sequential execution
            for (int i = 0; i < nrOfItems; ++i)
            {
               if (i == MAX_ITERATIONS)
               {
                  throw new ExecutionException(String.format(MAX_ITERATIONS_EXCEEDED, MAX_ITERATIONS));
               }
               final Iteration nodeIteration = Iteration.nr(i, this.iteration);
               this.executeNode(node, nodeIteration);
               if (nodeIteration.isTerminated())
               {
                  break;
               }
            }

            // TODO: support parallel execution?
         }
      }

      // Execute node once
      else
      {
         this.executeNode(node, this.iteration);
      }

      // Advance further
      this.advance();
   }

   private boolean condition(final Rule rule, final JsonObject businessData, final ProcessData processData)
   {
      return rule.match(this.evaluator, businessData, processData);
   }

   private void executeNode(final BaseNode<?> node, final Iteration iteration) throws ExecutionException
   {
      this.states.add(node.execute(this.getNextState(iteration)));
   }

   private void noOp(final BaseActivityNode<?> node, final String message)
   {
      LOG.warning(message);
      final State state = this.getNextState(this.iteration);
      state.setStarted(System.nanoTime());
      state.setPosition(node.getExit());
      state.setCompleted(System.nanoTime());
      this.states.add(state);
   }

   public long getDurationInMicros()
   {
      return this.getDuration(TimeUnit.MICROSECONDS);
   }

   public long getDurationInMillis()
   {
      return this.getDuration(TimeUnit.MILLISECONDS);
   }

   public long getDuration(final TimeUnit timeUnit)
   {
      return this.completion == null ? -1 : timeUnit.convert(this.completed - this.started, TimeUnit.NANOSECONDS);
   }

   public boolean completedNormally()
   {
      return this.completion != null && Completion.NORMAL.equals(this.completion);
   }

   /**
    * Returns a string representing the execution flow in the format <context>.<position>:<iteration>, where 'context' is the name of the node containing the executed (sub)process, 'position' is the
    * name of the flow exiting the executed node and 'iteration' is the direct or indirect (outer) iteration of the node execution
    *
    * @return
    */
   @JsonIgnore
   public String getPath()
   {
      return this.states.stream().map(state -> {
         final String ctx = state.getContext();
         final Flow pos = state.getPosition();
         final Iteration i = state.getIteration();
         return (ctx == null ? "" : ctx + ".") + pos.getName() + (i == null ? "" : ":" + i.value()) + (state.getExecution() != null ? " [" + state.getExecution().getPath() + "]" : "");
      }).collect(Collectors.joining(" -> "));
   }

   public enum Completion
   {
      NORMAL, FAILURE;
   }
}
