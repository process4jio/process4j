package io.process4j.core;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

import io.vertx.core.json.JsonObject;

public abstract class BaseProcess implements Process
{
   private static final String MSG_CONFLICTING_FLOW = "Flow with name %s already exists";
   private static final String MISSING_DEFAULT_POSITION = "Missing default (start) position";
   private static final String MISSING_EXIT_POSITION = "Missing exit position for node %s";
   private static final String TOO_MANY_EXIT_POSITIONS = "Too many exit positions for node %s";
   private static final String MSG_TOO_FEW_EXITS = "Too few exit positions for node %s";

   private Flow defaultPosition;
   private final Set<Flow> flows = Collections.synchronizedSet(new LinkedHashSet<>());
   boolean initialized;

   @Override
   public final Process init() throws InitializationException
   {
      if (this.initialized)
      {
         return this;
      }

      if (this.getDefaultPosition() == null)
      {
         throw new IllegalStateException(String.format(MISSING_DEFAULT_POSITION));
      }

      for (final Flow flow : this.getFlows())
      {
         if (flow.getTarget() != null)
         {
            flow.getTarget().init();
            this.checkNodePositions(flow.getTarget());
         }
      }

      this.initialized = true;

      return this;
   }

   @Override
   public final Execution execute(final JsonObject businessData, final ProcessData processData) throws ExecutionException
   {
      return this.execute(businessData, processData, Iteration.nr(0));
   }

   @Override
   public final Execution execute(final JsonObject businessData, final ProcessData processData, final Iteration iteration) throws ExecutionException
   {
      return this.execute(new State(null, this.defaultPosition, businessData, processData, iteration));
   }

   final Execution execute(final State state) throws ExecutionException
   {
      final Execution execution = new Execution(this.getClass().getCanonicalName());
      try
      {
         execution.start(state);
      }
      catch (final ExecutionException e)
      {
         execution.setException(e);
         execution.setCompletion(Execution.Completion.FAILURE);
         execution.setCompleted(System.nanoTime());
         throw new ExecutionException(String.format("An execution occured during execution of process '%s'", this.getClass().getCanonicalName()), e, execution);
      }
      execution.setCompletion(Execution.Completion.NORMAL);
      execution.setCompleted(System.nanoTime());
      return execution;
   }

   protected final Flow addFlow(final Flow flow)
   {
      // Duplicate
      if (!this.flows.add(flow))
      {
         throw new IllegalStateException(String.format(MSG_CONFLICTING_FLOW, flow.getName()));
      }

      final BaseNode<?> source = flow.getSource();

      // Exit position
      if (source != null)
      {
         source.exits.put(flow.getName(), flow);
      }

      // Default position
      else
      {
         this.defaultPosition = flow;
      }

      return flow;
   }

   final Flow getDefaultPosition()
   {
      return this.defaultPosition;
   }

   final Set<Flow> getFlows()
   {
      return this.flows;
   }

   private final void checkNodePositions(final BaseNode<?> node)
   {
      if (node.exits.isEmpty())
      {
         throw new IllegalStateException(String.format(MISSING_EXIT_POSITION, node.getName()));
      }

      // BaseActivityNode
      if (node instanceof BaseActivityNode<?>)
      {
         if (node.exits.size() > 1)
         {
            throw new IllegalStateException(String.format(TOO_MANY_EXIT_POSITIONS, node.getName()));
         }
      }

      // BaseGatewayNode
      else if (node.exits.size() < 2)
      {
         throw new IllegalStateException(String.format(MSG_TOO_FEW_EXITS, node.getName()));
      }
   }
}
