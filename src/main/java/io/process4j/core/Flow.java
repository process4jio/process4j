package io.process4j.core;

public final class Flow
{
   private final String id;
   private final String name;
   private final BaseNode<?> source;
   private final BaseNode<?> target;

   public static String getFlowId(final String id)
   {
      return "Flow_" + id.replaceAll(" ", "_");
   }

   public static Flow startFlow(final String name, final BaseNode<?> target)
   {
      return Flow.startFlow(Flow.getFlowId(name), name, target);
   }

   public static Flow startFlow(final String id, final String name, final BaseNode<?> target)
   {
      return new Flow(id, name, null, target);
   }

   public static Flow endFlow(final String name, final BaseNode<?> source)
   {
      return Flow.endFlow(Flow.getFlowId(name), name, source);
   }

   public static Flow endFlow(final String id, final String name, final BaseNode<?> source)
   {
      return new Flow(id, name, source, null);
   }

   public static Flow flow(final String name, final BaseNode<?> source, final BaseNode<?> target)
   {
      return Flow.flow(Flow.getFlowId(name), name, source, target);
   }

   public static Flow flow(final String id, final String name, final BaseNode<?> source, final BaseNode<?> target)
   {
      return new Flow(id, name, source, target);
   }

   private Flow(final String id, final String name, final BaseNode<?> source, final BaseNode<?> target)
   {
      this.id = id;
      this.name = name;
      this.source = source;
      this.target = target;
   }

   String getId()
   {
      return this.id;
   }

   String getName()
   {
      return this.name;
   }

   BaseNode<?> getSource()
   {
      return this.source;
   }

   BaseNode<?> getTarget()
   {
      return this.target;
   }
}
